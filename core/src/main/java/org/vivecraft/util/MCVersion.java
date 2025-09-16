package org.vivecraft.util;

import org.bukkit.Bukkit;
import org.vivecraft.ViveMain;

public class MCVersion {

    public static final MCVersion INVALID = new MCVersion(-1, -1);

    public final int major;
    public final int minor;
    public final String version;

    private static MCVersion CURRENT;

    private MCVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
        if (minor == 0) {
            this.version = "1." + this.major;
        } else {
            this.version = "1." + this.major + "." + this.minor;
        }
    }

    public static MCVersion getCurrent() {
        if (CURRENT == null) {
            String bukkitApiVersion = Bukkit.getBukkitVersion();
            String version = bukkitApiVersion.substring(0, bukkitApiVersion.indexOf("-"));
            CURRENT = parse(version, true);
        }
        return CURRENT;
    }

    public static MCVersion parse(String version, boolean fatal) {
        MCVersion mc;
        String[] segments = version.split("\\.");
        if (segments.length == 2) {
            mc = new MCVersion(Integer.parseInt(segments[1]), 0);
        } else if (segments.length == 3) {
            mc = new MCVersion(Integer.parseInt(segments[1]), Integer.parseInt(segments[2]));
        } else {
            if (fatal) {
                throw new RuntimeException("Vivecraft: Unrecognized mc version: " + version);
            } else {
                ViveMain.LOGGER.warning("Player send invalid version: " + version);
                return INVALID;
            }
        }
        return mc;
    }

    @Override
    public String toString() {
        return this.version;
    }
}
