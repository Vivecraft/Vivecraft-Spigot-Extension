package org.vivecraft;

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
        JsonObject allData;
        try (FileReader reader = new FileReader("src/main/resources/templates.json")) {
            allData = JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (args.length > 0 && args[0].equals("--clear")) {
            System.out.println("clearing previous generation");
            delete(stubs, false);
            delete(generatedMC, false);
        }

        Map<String, Set<String>> processedClasses = new HashMap<>();

        JsonObject templates = allData.getAsJsonObject("templates");
        JsonObject mappings = allData.getAsJsonObject("mappings");
        for (String key : templates.keySet()) {
            JsonObject root = templates.getAsJsonObject(key);
            // there needs to be an implementation for all mappings of this class,
            // and stubs need to be generated for this
            VersionLimit limit = parseVersionlimit(root);

            // generate stubs
            for (String clazz : mappings.keySet()) {
                generateStubs(limit, clazz, mappings.getAsJsonObject(clazz), processedClasses);
            }

            // generate classes for all mc versions
            writeAndRemapTemplate(limit, key, root, mappings);
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
        VersionLimit limit, String clazz, JsonObject content,
        Map<String, Set<String>> processedClasses) throws Exception
    {
        // iterate over all methods and collect needed imports
        Set<String> imports = new HashSet<>();
        List<MethodHolder> methods = new ArrayList<>();
        List<FieldHolder> fields = new ArrayList<>();
        List<ConstructorHolder> constructors = new ArrayList<>();

        if (content.has("methods")) {
            for (String methodName : content.getAsJsonObject("methods").keySet()) {
                Set<String> classes = new HashSet<>();
                JsonObject current = content.getAsJsonObject("methods").getAsJsonObject(methodName);

                VersionLimit versionLimit = VersionLimit.UNLIMITED;
                if (current.has("versions")) {
                    versionLimit = parseVersionlimit(current);
                }
                // type
                String type = current.get("type").getAsString();
                if (type.contains(".")) {
                    classes.add(type);
                }
                String method =
                    "    " + (current.has("access") ? current.get("access").getAsString() : "public") + " " + type +
                        " " + methodName + "(";
                char argName = 'a';
                boolean firstArg = true;

                // args
                for (JsonElement arg : current.getAsJsonArray("args")) {
                    if (arg.getAsString().contains(".")) {
                        classes.add(arg.getAsString().replace("...", ""));
                    }
                    method += (firstArg ? "" : ", ") + arg.getAsString() + " " + (argName++);
                    firstArg = false;
                }
                method += ") {\n        throw new AssertionError();\n    }\n\n";
                methods.add(new MethodHolder(method, methodName, clazz, classes, versionLimit));
                //imports.addAll(classes);
            }
        }

        if (content.has("fields")) {
            for (String fieldName : content.getAsJsonObject("fields").keySet()) {
                JsonObject current = content.getAsJsonObject("fields").getAsJsonObject(fieldName);

                // type
                String type = current.get("type").getAsString();
                if (type.contains(".")) {
                    imports.add(type);
                }

                String field = "    public ";
                if (current.has("static")) {
                    field += "static ";
                }
                field += type + " " + fieldName + ";\n\n";
                fields.add(new FieldHolder(field, fieldName, clazz, type));
            }
        }

        if (content.has("constructors")) {
            for (String con : content.getAsJsonObject("constructors").keySet()) {
                Set<String> classes = new HashSet<>();

                String code = "    public " + clazz + "(";
                char argName = 'a';
                boolean firstArg = true;

                // args
                for (JsonElement arg : content.getAsJsonObject("constructors").getAsJsonArray(con)) {
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
                String code = "package " + parentPackage + ";\n";

                if (!imports.isEmpty()) {
                    code += "\n";
                }

                for (String imp : imports) {
                    code += "import " + getClass(imp, version) + ";\n";
                }

                code += "\npublic class " + parentName;
                if (content.has("parent")) {
                    code += " extends " + getClass(content.get("parent").getAsString(), version);
                }
                code += " {\n";

                if (!fields.isEmpty()) {
                    code += "\n";
                }

                for (FieldHolder field : fields) {
                    String line = field.code;
                    if (field.type.contains(".")) {
                        line = line.replace(field.type, getClass(field.type, version));
                    }
                    line = line.replace(field.name, getField(field.clazz, field.name, version));
                    code += line;
                }

                if (!constructors.isEmpty()) {
                    code += "\n";
                }

                for (ConstructorHolder con : constructors) {
                    String line = con.code;
                    line = line.replace(con.clazz, parentName);
                    for (String c : con.argClasses) {
                        line = line.replace(c, getClass(c, version));
                    }
                    code += line;
                }

                if (!methods.isEmpty()) {
                    code += "\n";
                }

                for (MethodHolder method : methods) {
                    String line = method.code;
                    for (String c : method.argClasses) {
                        if (!c.contains(".")) continue;
                        line = line.replace(c, getClass(c, version));
                    }
                    if (method.limit.valid(version)) {
                        line = line.replace(method.name, getMethod(method.clazz, method.name, version));
                    }
                    code += line;
                }

                code += "}\n";
                writer.write(code);
            }
            writtenFiles.add(parentClass);
        }
    }

    private static void writeAndRemapTemplate(
        VersionLimit limit, String template, JsonObject templateData, JsonObject mappings) throws IOException
    {
        List<String> lines = Files.readAllLines(
            new File("src/main/java/org/vivecraft/compat_impl/mc_X_X/" + template).toPath());
        List<String> validClasses = templateData.get("mappingClasses").getAsJsonArray().asList().stream()
            .map(JsonElement::getAsString).collect(Collectors.toList());
        for (String version : getVersions(templateData.get("parent").getAsString())) {
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

                for (String clazz : mappings.keySet().stream()
                    .sorted(Comparator.comparing(String::length).reversed()).collect(Collectors.toList())) {
                    if (!parseVersionlimit(mappings.getAsJsonObject(clazz)).valid(mc) ||
                        !validClasses.contains(clazz))
                    {
                        continue;
                    }
                    code = code.replace(clazz, getClass(clazz, version));
                }

                for (String clazz : mappings.keySet()) {
                    if (!parseVersionlimit(mappings.getAsJsonObject(clazz)).valid(mc) ||
                        !validClasses.contains(clazz))
                    {
                        continue;
                    }
                    JsonObject c = mappings.getAsJsonObject(clazz);
                    if (c.has("methods")) {
                        for (String method : c.getAsJsonObject("methods").keySet()) {
                            JsonObject m = c.getAsJsonObject("methods").getAsJsonObject(method);
                            if (m.has("versions") && !parseVersionlimit(m).valid(version)) continue;
                            code = code.replace(method, getMethod(clazz, method, version));
                        }
                    }
                    if (c.has("fields")) {
                        for (String field : c.getAsJsonObject("fields").keySet()) {
                            JsonObject f = c.getAsJsonObject("fields").getAsJsonObject(field);
                            if (f.has("versions") && !parseVersionlimit(f).valid(version)) continue;
                            code = code.replace(field, getField(clazz, field, version));
                        }
                    }
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

    private static VersionLimit parseVersionlimit(JsonObject root) {
        if (root.has("versions")) {
            String from = null;
            String to = null;
            if (root.getAsJsonObject("versions").has("from")) {
                from = root.getAsJsonObject("versions").get("from").getAsString();
            }
            if (root.getAsJsonObject("versions").has("to")) {
                to = root.getAsJsonObject("versions").get("to").getAsString();
            }
            return new VersionLimit(from, to);
        } else {
            return VersionLimit.UNLIMITED;
        }
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
            Class.forName(clazz.replace("...", ""));
            return clazz;
        } catch (Exception ignore) {}
        return Objects.requireNonNull(
                Objects.requireNonNull(Mappings.LOOKUP.getClass(clazz), "no mapping for class: " + clazz)
                    .getMappings(version), "no mapping of class: " + clazz + " with version: " + version)
            .get(getNameSpace(version));
    }

    private static String getMethod(String clazz, String method, String version) {
        String[] parts = method.split("_");
        int index = 0;
        if (parts.length > 1) {
            index = Integer.parseInt(parts[1]);
        }
        return Objects.requireNonNull(Objects.requireNonNull(
                    Objects.requireNonNull(Mappings.LOOKUP.getClass(clazz), "no mapping for class: " + clazz)
                        .getMethod(parts[0], index), "no mapping of method: " + method + " in class: " + clazz)
                .getName(version, getNameSpace(version)),
            "no mapping of method: " + method + " in class: " + clazz + " with version: " + version).getName();
    }

    private static String getField(String clazz, String field, String version) {
        String[] parts = field.split("_");
        int index = 0;
        if (parts.length > 1) {
            index = Integer.parseInt(parts[1]);
        }
        return Objects.requireNonNull(Objects.requireNonNull(
                    Objects.requireNonNull(Mappings.LOOKUP.getClass(clazz), "no mapping for class: " + clazz)
                        .getField(parts[0], index), "no mapping of field: " + field + " in class: " + clazz)
                .getName(version, getNameSpace(version)),
            "no mapping of field: " + field + " in class: " + clazz + " with version: " + version);
    }

    private static List<String> getVersions(String clazz) {
        return Objects.requireNonNull(Mappings.LOOKUP.getClass(clazz), "no mapping for class: " + clazz).getMappings()
            .keySet().stream().sorted(Comparator.comparing(v -> MCVersion.parse((String) v, true)).reversed())
            .collect(Collectors.toList());
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
        public final VersionLimit limit;

        public MethodHolder(String code, String name, String clazz, Set<String> argClasses, VersionLimit limit) {
            this.code = code;
            this.name = name;
            this.clazz = clazz;
            this.argClasses = argClasses;
            this.limit = limit;
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
