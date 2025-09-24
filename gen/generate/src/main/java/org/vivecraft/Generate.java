package org.vivecraft;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.vivecraft.accessors.Mappings;
import org.vivecraft.util.MCVersion;
import org.vivecraft.util.Utils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

public class Generate {

    private static final File stubs = new File("../stubs/src/main/java");
    private static final File generatedMC = new File("../GeneratedMC");

    private static final File spigotGradle = new File("src/main/resources/spigot-build.gradle");
    private static final File mojangGradle = new File("src/main/resources/mojang-build.gradle");

    public static void main(String[] args) throws Exception {
        JsonObject templates;
        try (FileReader reader = new FileReader("src/main/resources/templates.json")) {
            templates = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (args.length > 0 && args[0].equals("--clear")) {
            System.out.println("clearing previous generation");
            delete(stubs, false);
            delete(generatedMC, false);
        }

        Map<String, Set<String>> processedClasses = new HashMap<>();

        for (String key : templates.keySet()) {
            JsonObject root = templates.getAsJsonObject(key);
            // there needs to be an implementation for all mappings of this class,
            // and stubs need to be generated for this
            VersionLimit limit;
            if (root.has("versions")) {
                String from = null;
                String to = null;
                if (root.getAsJsonObject("versions").has("from")) {
                    from = root.getAsJsonObject("versions").get("from").getAsString();
                }
                if (root.getAsJsonObject("versions").has("to")) {
                    to = root.getAsJsonObject("versions").get("to").getAsString();
                }
                limit = new VersionLimit(from, to);
            } else {
                limit = VersionLimit.UNLIMITED;
            }

            // generate parent stubs
            generateStubs(limit, root.getAsJsonObject("parent"), processedClasses);

            generateBasicStubs(limit, root.getAsJsonObject("mappings").get("classes").getAsJsonArray(),
                processedClasses);

            // generate classes for all mc versions
            writeAndRemapTemplate(limit, key, root.getAsJsonObject("parent"),
                root.getAsJsonObject("mappings"));
        }

        System.out.println("generation finished");
    }

    private static void delete(File dir, boolean deleteItself) {
        if (dir.exists()) {
            if (dir.isDirectory()) {
                for (File child : dir.listFiles()) {
                    delete(child, true);
                }
            }
            if (deleteItself) {
                dir.delete();
            }
        }
    }

    private static void generateStubs(
        VersionLimit limit, JsonObject parent, Map<String, Set<String>> processedClasses) throws Exception
    {
        // iterate over all methods and collect needed imports
        String clazz = parent.get("class").getAsString();
        Set<String> imports = new HashSet<>();
        List<MethodHolder> methods = new ArrayList<>();
        List<FieldHolder> fields = new ArrayList<>();
        List<ConstructorHolder> constructors = new ArrayList<>();

        for (String methodName : parent.getAsJsonObject("methods").keySet()) {
            Set<String> classes = new HashSet<>();
            JsonObject current = parent.getAsJsonObject("methods").getAsJsonObject(methodName);

            // type
            String type = current.get("type").getAsString();
            if (type.contains(".")) {
                classes.add(type);
            }
            String method = "    " + (current.has("access") ? current.get("access").getAsString() : "public") + " " + type + " " + methodName + "(";
            char argName = 'a';
            boolean firstArg = true;

            // args
            for (JsonElement arg : current.getAsJsonArray("args")) {
                if (arg.getAsString().contains(".")) {
                    classes.add(arg.getAsString());
                }
                method += (firstArg ? "" : ", ") + arg.getAsString() + " " + (argName++);
                firstArg = false;
            }
            method += ") {\n        throw new AssertionError();\n    }\n\n";
            methods.add(new MethodHolder(method, methodName, current.get("class").getAsString(), classes));
            imports.addAll(classes);
        }

        for (String fieldName : parent.getAsJsonObject("fields").keySet()) {
            JsonObject current = parent.getAsJsonObject("fields").getAsJsonObject(fieldName);

            // type
            String type = current.get("type").getAsString();
            if (type.contains(".")) {
                imports.add(type);
            }
            String field = "    public " + type + " " + fieldName + ";\n\n";
            fields.add(new FieldHolder(field, fieldName, current.get("class").getAsString(), type));
        }

        for (String con : parent.getAsJsonObject("constructors").keySet()) {
            Set<String> classes = new HashSet<>();

            String code = "    public " + clazz + "(";
            char argName = 'a';
            boolean firstArg = true;

            // args
            for (JsonElement arg : parent.getAsJsonObject("constructors").getAsJsonArray(con)) {
                if (arg.getAsString().contains(".")) {
                    classes.add(arg.getAsString());
                }
                code += (firstArg ? "" : ", ") + arg.getAsString() + " " + (argName++);
                firstArg = false;
            }
            code += ") {}\n\n";
            constructors.add(new ConstructorHolder(code, clazz, classes));
            imports.addAll(classes);
        }

        // writing file
        Set<String> writtenFiles = new HashSet<>();
        for (String version : getVersions(clazz)) {
            if (!limit.valid(version)) continue;
            addCreate(processedClasses, version, clazz);
            String parentClass = getClass(clazz, version);
            if (writtenFiles.contains(parentClass)) {
                continue;
            }
            String parentPackage = parentClass.substring(0, parentClass.lastIndexOf('.'));
            String parentName = parentClass.substring(parentPackage.length() + 1);

            File target = new File(stubs, parentClass.replace(".", "/") + ".java");
            target.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(target)) {
                String content = "package " + parentPackage + ";\n";

                if (!imports.isEmpty()) {
                    content += "\n";
                }

                for (String imp : imports) {
                    content += "import " + getClass(imp, version) + ";\n";
                }

                content += "\npublic class " + parentName;
                if (parent.has("parent")) {
                    content += " extends " + parent.get("parent").getAsString();
                }
                content += " {\n";

                if (!fields.isEmpty()) {
                    content += "\n";
                }

                for (FieldHolder field : fields) {
                    String line = field.code;
                    if (field.type.contains(".")) {
                        line = line.replace(field.type, getClass(field.type, version));
                    }
                    line = line.replace(field.name, getField(field.clazz, field.name, version));
                    content += line;
                }

                if (!constructors.isEmpty()) {
                    content += "\n";
                }

                for (ConstructorHolder con : constructors) {
                    String line = con.code;
                    line = line.replace(con.clazz, parentName);
                    for (String c : con.argClasses) {
                        line = line.replace(c, getClass(c, version));
                    }
                    content += line;
                }

                if (!methods.isEmpty()) {
                    content += "\n";
                }

                for (MethodHolder method : methods) {
                    String line = method.code;
                    for (String c : method.argClasses) {
                        line = line.replace(c, getClass(c, version));
                    }
                    line = line.replace(method.name, getMethod(method.clazz, method.name, version));
                    content += line;
                }

                content += "}\n";
                writer.write(content);
            }
            writtenFiles.add(version);
        }
    }

    private static void generateBasicStubs(
        VersionLimit limit, JsonArray classes, Map<String, Set<String>> processedClasses) throws Exception
    {

        // writing file
        Set<String> writtenFiles = new HashSet<>();
        for (JsonElement element : classes) {
            String clazz = element.getAsString();
            for (String version : getVersions(clazz)) {
                if (processedClasses.getOrDefault(version, new HashSet<>()).contains(clazz)) {
                    continue;
                }
                if (!limit.valid(version)) continue;
                addCreate(processedClasses, version, clazz);
                String parentClass = getClass(clazz, version);
                if (writtenFiles.contains(parentClass)) {
                    continue;
                }
                String parentPackage = parentClass.substring(0, parentClass.lastIndexOf('.'));
                String parentName = parentClass.substring(parentPackage.length() + 1);

                File target = new File(stubs, parentClass.replace(".", "/") + ".java");
                target.getParentFile().mkdirs();
                try (FileWriter writer = new FileWriter(target)) {
                    String content = "package " + parentPackage + ";\n";
                    content += "\npublic abstract class " + parentName + " {}\n";
                    writer.write(content);
                }
                writtenFiles.add(version);
            }
        }
    }

    private static void writeAndRemapTemplate(
        VersionLimit limit, String template, JsonObject parent, JsonObject mappings) throws IOException
    {
        List<String> lines = Files.readAllLines(
            new File("src/main/java/org/vivecraft/compat_impl/mc_X_X/" + template).toPath());
        for (String version : getVersions(parent.get("class").getAsString())) {
            MCVersion mc = MCVersion.parse(version, true);
            if (!limit.valid(mc)) continue;

            String code = preprocessLines(lines, mc);
            File target = new File(generatedMC,
                mc.version_ + "_gen/src/main/java/org/vivecraft/compat_impl/mc_" + mc.version_ + "/" + template);
            target.getParentFile().mkdirs();

            // copy build.gradle
            copyFile(usesMojang(version) ? mojangGradle : spigotGradle,
                new File(generatedMC, mc.version_ + "_gen/build.gradle"), Utils.MapOf("XX_XX", version));

            try (FileWriter writer = new FileWriter(target)) {
                code = code.replace("mc_X_X", "mc_" + mc.version_);

                for (String clazz : mappings.getAsJsonArray("classes").asList().stream().map(JsonElement::getAsString)
                    .sorted(Comparator.comparing(String::length).reversed()).collect(Collectors.toList())) {
                    code = code.replace(clazz, getClass(clazz, version));
                }
                for (String method : mappings.getAsJsonObject("methods").keySet()) {
                    code = code.replace(method,
                        getMethod(mappings.getAsJsonObject("methods").get(method).getAsString(), method, version));
                }
                // also remap methods from the parent
                for (String method : parent.getAsJsonObject("methods").keySet()) {
                    code = code.replace(method,
                        getMethod(parent.getAsJsonObject("methods").getAsJsonObject(method).get("class").getAsString(),
                            method, version));
                }
                // and fields
                for (String field : parent.getAsJsonObject("fields").keySet()) {
                    code = code.replace(field,
                        getField(parent.getAsJsonObject("fields").getAsJsonObject(field).get("class").getAsString(),
                            field, version));
                }
                writer.write(code);
            }
        }
    }

    private static String preprocessLines(List<String> lines, MCVersion mc) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            if (line.startsWith("//#")) {
                String condition = line.substring(0, line.lastIndexOf("#") + 1);
                String[] versions = condition.replace("/", "").replace("#", "").split("-");
                if (!new VersionLimit(versions[0], versions[1]).valid(mc)) {
                    continue;
                }
                line = line.substring(condition.length());
            }
            sb.append(line).append("\n");
        }
        return sb.toString();
    }

    private static void copyFile(File src, File dst, Map<String, String> replacements) throws IOException {
        String file = String.join("\n", Files.readAllLines(src.toPath()));

        try (FileWriter writer = new FileWriter(dst)) {
            for (String key : replacements.keySet()) {
                file = file.replace(key, replacements.get(key));
            }
            writer.write(file);
        }
    }

    private static String getClass(String clazz, String version) {
        try {
            // don't try to remap java native classes
            Class.forName(clazz);
            return clazz;
        } catch (Exception ignore) {}
        return Objects.requireNonNull(
                Objects.requireNonNull(Mappings.LOOKUP.getClass(clazz), "no mapping for class: " + clazz)
                    .getMappings(version), "no mapping of class: " + clazz + " with version: " + version)
            .get(getNameSpace(version));
    }

    private static String getMethod(String clazz, String method, String version) {
        return Objects.requireNonNull(Objects.requireNonNull(
                    Objects.requireNonNull(Mappings.LOOKUP.getClass(clazz), "no mapping for class: " + clazz)
                        .getMethod(method, 0), "no mapping of method: " + method + " in class: " + clazz)
                .getName(version, getNameSpace(version)),
            "no mapping of method: " + method + " in class: " + clazz + " with version: " + version).getName();
    }

    private static String getField(String clazz, String field, String version) {
        return Objects.requireNonNull(Objects.requireNonNull(
                    Objects.requireNonNull(Mappings.LOOKUP.getClass(clazz), "no mapping for class: " + clazz)
                        .getField(field, 0), "no mapping of field: " + field + " in class: " + clazz)
                .getName(version, getNameSpace(version)),
            "no mapping of field: " + field + " in class: " + clazz + " with version: " + version);
    }

    private static Set<String> getVersions(String clazz) {
        return Objects.requireNonNull(Mappings.LOOKUP.getClass(clazz), "no mapping for class: " + clazz).getMappings()
            .keySet();
    }

    private static String getNameSpace(String version) {
        return usesMojang(version) ? "mojang" : "spigot";
    }

    private static boolean usesMojang(String version) {
        return MCVersion.parse(version, true).major >= 17;
    }

    private static void addCreate(Map<String, Set<String>> map, String key, String value) {
        Set<String> set = map.getOrDefault(key, new HashSet<>());
        set.add(value);
        map.put(key, set);
    }

    private static class MethodHolder {
        public final String code;
        public final String name;
        public final String clazz;
        public final Set<String> argClasses;

        public MethodHolder(String code, String name, String clazz, Set<String> argClasses) {
            this.code = code;
            this.name = name;
            this.clazz = clazz;
            this.argClasses = argClasses;
        }
    }

    private static class FieldHolder {
        public final String code;
        public final String name;
        public final String clazz;
        public final String type;

        public FieldHolder(String code, String name, String clazz, String type) {
            this.code = code;
            this.name = name;
            this.clazz = clazz;
            this.type = type;
        }
    }

    private static class ConstructorHolder {
        public final String code;
        public final String clazz;
        public final Set<String> argClasses;

        public ConstructorHolder(String code, String clazz, Set<String> argClasses) {
            this.code = code;
            this.clazz = clazz;
            this.argClasses = argClasses;
        }
    }

    private static class VersionLimit {

        public static VersionLimit UNLIMITED = new VersionLimit();

        MCVersion from;
        MCVersion to;

        private VersionLimit() {
            this.from = MCVersion.INVALID;
            this.to = MCVersion.MAX;
        }

        public VersionLimit(String from, String to) {
            if (from != null) {
                this.from = MCVersion.parse(from, true);
            } else {
                this.from = MCVersion.INVALID;
            }
            if (to != null) {
                this.to = MCVersion.parse(to, true);
            } else {
                this.to = MCVersion.MAX;
            }
        }

        public boolean valid(String version) {
            return valid(MCVersion.parse(version, true));
        }

        public boolean valid(MCVersion version) {
            return this.from.compareTo(version) <= 0 && this.to.compareTo(version) >= 0;
        }
    }
}
