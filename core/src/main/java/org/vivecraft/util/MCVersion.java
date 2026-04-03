package org.vivecraft.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;
import org.vivecraft.ViveMain;

public class MCVersion implements Comparable<MCVersion> {

    public static final MCVersion INVALID = new MCVersion(-1, -1, -1);
    public static final MCVersion MAX = new MCVersion(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE);

    public final int major;
    public final int minor;
    public final int patch;
    public final String version;
    public final String version_;

    private static MCVersion CURRENT;
    private static MCVersion CORRECTED;

    private MCVersion(int minor, int patch) {
        this(1, minor, patch);
    }

    public MCVersion(int major, int minor, int patch) {
        this.major = major;
        this.minor = minor;
        this.patch = patch;
        if (patch == 0) {
            this.version = major + "." + this.minor;
        } else {
            this.version = major + "." + this.minor + "." + this.patch;
        }
        this.version_ = this.version.replace(".", "_");
    }

    public static MCVersion getCurrent() {
        if (CURRENT == null) {
            String bukkitApiVersion = Bukkit.getBukkitVersion();
            String version = bukkitApiVersion.substring(0, bukkitApiVersion.indexOf("-"));
            CURRENT = parse(version, true);
        }
        return CURRENT;
    }

    public static MCVersion getCurrentCorrected() {
        if (CORRECTED == null) {
            MCVersion version = getCurrent();
            // version resolving always tries to use version before the current version, if the actual one is not available
            // some versions are not available, because they had critical bugfixes, so those need to use the one after
            if (version.is(1, 9, 1)) {
                version = new MCVersion(9, 2);
            } else if (version.is(1, 9, 3)) {
                version = new MCVersion(9, 4);
            } else if (version.is(1, 16, 0)) {
                version = new MCVersion(16, 1);
            } else if (version.is(1, 20, 0)) {
                version = new MCVersion(20, 1);
            } else if (version.is(1, 20, 3)) {
                version = new MCVersion(20, 4);
            } else if (version.is(1, 20, 5)) {
                version = new MCVersion(20, 6);
            } else if (version.is(1, 21, 2)) {
                version = new MCVersion(21, 3);
            }
            CORRECTED = version;
        }
        return CORRECTED;
    }

    public static MCVersion parse(String version, boolean fatal) {
        MCVersion mc;
        String[] segments = version.split("\\.");
        if (segments.length == 2) {
            mc = new MCVersion(Integer.parseInt(segments[0]), Integer.parseInt(segments[1]), 0);
        } else if (segments.length == 3) {
            mc = new MCVersion(Integer.parseInt(segments[0]), Integer.parseInt(segments[1]), Integer.parseInt(segments[2]));
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

    public boolean is(int major, int minor, int patch) {
        return this.minor == major && this.patch == minor && this.patch == patch;
    }

    @Override
    public String toString() {
        return this.version;
    }

    @Override
    public int compareTo(@NotNull MCVersion o) {
        if (this.major < o.major) {
            return -1;
        } else if (this.major > o.major) {
            return 1;
        } else if (this.minor < o.minor) {
            return -1;
        } else if (this.minor > o.minor) {
            return 1;
        } else if (this.patch < o.patch) {
            return -1;
        } else if (this.patch > o.patch) {
            return 1;
        } else {
            return 0;
        }
    }
}
