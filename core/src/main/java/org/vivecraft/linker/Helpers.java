package org.vivecraft.linker;

import org.vivecraft.ViveMain;
import org.vivecraft.compat.ApiHelper;
import org.vivecraft.compat.McHelper;
import org.vivecraft.compat.NMSHelper;
import org.vivecraft.util.MCVersion;

import java.util.logging.Level;

/**
 * gets the correct implementation of the helpers, for the current server
 * uses reflection, because it wouldn't compile otherwise
 */
public class Helpers {

    public static ApiHelper getApi() {
        return (ApiHelper) getHelper("org.vivecraft.compat_impl.mc_%s.Api_%s");
    }

    public static McHelper getMc() {
        return (McHelper) getHelper("org.vivecraft.compat_impl.mc_%s.Mc_%s");
    }

    public static NMSHelper getNMS() {
        return (NMSHelper) getHelper("org.vivecraft.compat_impl.mc_%s.NMS_%s");
    }

    public static Object getHelper(String classTemplate) {
        MCVersion mc = MCVersion.getCurrent();
        int major = mc.major;
        int minor = mc.minor;
        while (major > 7) {
            while (minor >= 0) {
                String compatVersion;
                if (minor == 0) {
                    compatVersion = "1_" + major;
                } else {
                    compatVersion = "1_" + major + "_" + minor;
                }
                try {
                    Class<?> helperClass = Class.forName(String.format(classTemplate, compatVersion, compatVersion));
                    try {
                        return helperClass.getConstructor().newInstance();
                    } catch (Exception e) {
                        ViveMain.LOGGER.log(Level.SEVERE, "could not instantiate " + helperClass.getName(), e);
                        throw new RuntimeException(e);
                    }
                } catch (ClassNotFoundException ignore) {}
                minor--;
            }
            minor = 10;
            major--;
        }
        throw new RuntimeException("Couldn't find any compatible class for " + classTemplate);
    }
}
