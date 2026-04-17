package org.vivecraft.linker;

import org.vivecraft.ViveMain;
import org.vivecraft.compat.ApiHelper;
import org.vivecraft.compat.McHelper;
import org.vivecraft.compat.NMSHelper;
import org.vivecraft.debug.Debug;
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
        MCVersion mc = MCVersion.getCurrentCorrected();
        int major = mc.major;
        int minor = mc.minor;
        int patch = mc.patch;
        while (major >= 1) {
            while (minor >= 0) {
                while (patch >= 0) {
                    String compatVersion;
                    if (patch == 0) {
                        compatVersion = major + "_" + minor;
                    } else {
                        compatVersion = major + "_" + minor + "_" + patch;
                    }
                    try {
                        Class<?> helperClass = Class.forName(
                            String.format(classTemplate, compatVersion, compatVersion));
                        try {
                            Debug.log("using helper: %s", helperClass.getName());
                            return helperClass.getConstructor().newInstance();
                        } catch (Exception e) {
                            ViveMain.LOGGER.log(Level.SEVERE, "could not instantiate " + helperClass.getName(), e);
                            throw new RuntimeException(e);
                        }
                    } catch (ClassNotFoundException ignore) {}
                    patch--;
                }
                patch = 11;
                minor--;
            }
            minor = 21;
            patch = 11;
            major--;
        }
        throw new RuntimeException("Couldn't find any compatible class for " + classTemplate);
    }
}
