package org.vivecraft.util;

import org.vivecraft.debug.Debug;

public class ViveVersion implements Comparable<ViveVersion> {

    public final static ViveVersion UNKNOWN = new ViveVersion();

    public final String fullVersion;

    private int major;
    private int minor;
    private int patch;
    private int alpha = 0;
    private int beta = 0;
    private boolean featureTest = false;

    private boolean unknown = false;

    private ViveVersion() {
        this.fullVersion = "Unknown";
        this.unknown = true;
    }

    public ViveVersion(String version) {
        this.fullVersion = version;
        try {
            String[] versionParts = version.split(" ");
            String vive;
            if (versionParts.length == 2) {
                // 1.0.0+ version scheme
                // versions sent look like "Vivecraft-[mc version]-[mod loader]-[vive version]-(a/b/test) VR/NONVR"
                vive = versionParts[0];
            } else {
                // versions sent look like "Vivecraft [mc version] (jrbudda)-VR/NONVR-[mod loader]-[vive version]"
                // or for the standalone, this is not parsable
                // versions sent look like "Vivecraft [mc version] (jrbudda)-VR/NONVR-[feature]-[releases]"
                vive = versionParts[versionParts.length - 1];
            }
            String[] parts = vive.split("-");
            int viveVersionIndex = parts.length - 1;
            if (!parts[viveVersionIndex].contains(".")) {
                viveVersionIndex = parts.length - 2;
                String testString = parts[parts.length - 1];
                // prerelease
                if (testString.matches("a\\d+.*")) {
                    this.alpha = Integer.parseInt(testString.replaceAll("\\D+", ""));
                } else if (testString.matches("b\\d+.*")) {
                    this.beta = Integer.parseInt(testString.replaceAll("\\D+", ""));
                }
                // if the prerelease string is not just aXX or bXX it's a feature test as well and ranked slightly higher
                if (!testString.replaceAll("^[ab]\\d+", "").isEmpty()) {
                    this.featureTest = true;
                }
            }
            String[] ints = parts[viveVersionIndex].split("\\.");
            // remove all letters, since stupid me put a letter in one version
            this.major = Integer.parseInt(ints[0].replaceAll("\\D+", ""));
            this.minor = Integer.parseInt(ints[1].replaceAll("\\D+", ""));
            this.patch = Integer.parseInt(ints[2].replaceAll("\\D+", ""));
        } catch (Exception e) {
            // couldn't parse the version, mark as unknown
            Debug.log("coudln't parse vivecraft version: %s, Error: %s", version, e);
            this.unknown = true;
        }
    }

    public boolean isValid() {
        return !this.unknown;
    }

    /**
     * returns 1 if the other version is newer, -1 if the other version is older. 0 if they are equal
     */
    @Override
    public int compareTo(ViveVersion o) {
        long result = this.compareNumber() - o.compareNumber();
        if (result < 0) {
            return 1;
        } else if (result == 0L) {
            return 0;
        }
        return -1;
    }

    // two digits per segment, should be enough right?
    private long compareNumber() {
        if (this.unknown) return -1;
        // digit flag
        // major minor patch full release beta alpha feature test
        // 00    00    00    0            00   00    0
        return (this.featureTest ? 1L : 0L) +
            this.alpha * 10L +
            this.beta * 1000L +
            (this.alpha + this.beta == 0 ? 10000L : 0L) +
            this.patch * 1000000L +
            this.minor * 100000000L +
            this.major * 10000000000L;
    }

    public String versionString() {
        String version = this.major + "." + this.minor + "." + this.patch;
        if (this.alpha > 0) {
            version += "-a" + this.alpha;
        }
        if (this.beta > 0) {
            version += "-b" + this.beta;
        }
        if (this.featureTest) {
            version += "_featuretest";
        }
        return version;
    }

    @Override
    public String toString() {
        if (this.unknown) {
            return this.fullVersion + "(unknown format)";
        }

        return this.fullVersion + "(" + versionString() + ")";
    }
}
