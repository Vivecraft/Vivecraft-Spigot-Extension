package org.vivecraft.util.reflection;

import org.vivecraft.util.MCVersion;

public class ClassGetter {

    public static Class<?> getRaw(String cls) throws ClassNotFoundException {
        return Class.forName(cls);
    }

    public static Class<?> getWithApi(String pre, String post) throws ClassNotFoundException {
        MCVersion mc = MCVersion.getCurrent();
        for (int i = 0; i <= 10; i++) {
            String apiClass;
            if (i != 10) {
                apiClass = String.format("%s.v1_%s_R%s.%s", pre, mc.major, i, post);
            } else {
                apiClass = pre + "." + post;
            }
            try {
                return getRaw(apiClass);
            } catch (ClassNotFoundException ignored) {}
        }

        throw new ClassNotFoundException("couldn't find any class matching " + pre + ".###." + post);
    }
}
