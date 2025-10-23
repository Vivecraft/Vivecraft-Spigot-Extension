package org.vivecraft.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jetbrains.annotations.Nullable;
import org.vivecraft.ViveMain;
import org.vivecraft.compat.Platform;
import org.vivecraft.config.enums.UpdateType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class UpdateChecker {

    public static boolean HAS_UPDATE = false;

    public static String CHANGELOG = "";

    public static String NEWEST_VERSION = "";

    private static long LAST_UPDATE_CHECK = 0;
    private static UpdateType LAST_UPDATE_CHECK_TYPE = UpdateType.ALPHA;

    public static void scheduleUpdateCheck(@Nullable Consumer<String> notifier) {
        // check for update on not the main thread
        Platform.getInstance().getScheduler().runAsync(() -> {
            if (checkForUpdates() && notifier != null) {
                Platform.getInstance().getScheduler().runGlobal(
                    () -> notifier.accept(ViveMain.translate("vivecraft.plugin.update",
                        Utils.green(UpdateChecker.NEWEST_VERSION))));
            }
        });
    }

    public static boolean checkForUpdates() {
        if (LAST_UPDATE_CHECK + TimeUnit.DAYS.toMillis(1) > System.currentTimeMillis() &&
            LAST_UPDATE_CHECK_TYPE == ViveMain.CONFIG.updateType.get())
        {
            // last check less than a day old, don't need to recheck imo
            return HAS_UPDATE;
        }

        LAST_UPDATE_CHECK = System.currentTimeMillis();
        ViveMain.LOGGER.info("Checking for Updates");

        UpdateType updateType = ViveMain.CONFIG.updateType.get();

        try {
            String apiURL = "https://api.modrinth.com/v2/project/vivecraft-spigot-extension/version";
            HttpURLConnection conn = (HttpURLConnection) new URL(apiURL).openConnection();
            // 10 seconds read and connect timeout
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Accept", "application/json,*/*");
            conn.connect();

            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                ViveMain.LOGGER.warning("Error '" + conn.getResponseCode() + "' fetching updates");
                return false;
            }

            JsonElement j = new JsonParser().parse(inputStreamToString(conn.getInputStream()));

            List<Version> versions = new LinkedList<>();

            if (j.isJsonArray()) {
                for (JsonElement element : j.getAsJsonArray()) {
                    if (element.isJsonObject()) {
                        JsonObject obj = element.getAsJsonObject();
                        versions.add(
                            new Version(obj.get("name").getAsString(),
                                obj.get("version_number").getAsString(),
                                obj.get("changelog").getAsString()));
                    }
                }
            }
            // sort the versions, modrinth doesn't guarantee them to be sorted.
            Collections.sort(versions);

            String currentVersionNumber = ViveMain.INSTANCE.getDescription().getVersion();
            Version current = new Version(currentVersionNumber, currentVersionNumber, "");

            // enforce update notifications if using a non release
            if (current.alpha > 0 && updateType != UpdateType.ALPHA) {
                updateType = UpdateType.ALPHA;
            } else if (current.beta > 0 && updateType != UpdateType.BETA) {
                updateType = UpdateType.BETA;
            }

            LAST_UPDATE_CHECK_TYPE = updateType;

            StringBuilder sb = new StringBuilder();
            NEWEST_VERSION = "";

            for (Version v : versions) {
                if (v.isVersionType(updateType) && current.compareTo(v) > 0) {
                    sb.append(Utils.green(v.fullVersion)).append(": \n").append(v.changelog).append("\n\n");
                    if (NEWEST_VERSION.isEmpty()) {
                        NEWEST_VERSION = v.fullVersion;
                    }
                    HAS_UPDATE = true;
                }
            }
            // no carriage returns please
            CHANGELOG = sb.toString().replaceAll("\\r", "");
            if (HAS_UPDATE) {
                ViveMain.LOGGER.info("Vivecraft update found: " + NEWEST_VERSION);
            }
        } catch (IOException e) {
            ViveMain.LOGGER.log(Level.WARNING, "Error fetching available vivecraft updates: ", e);
        }
        return HAS_UPDATE;
    }

    private static String inputStreamToString(InputStream inputStream) {
        return new BufferedReader(new InputStreamReader(inputStream))
            .lines().collect(Collectors.joining("\n"));
    }

    private static class Version implements Comparable<Version> {

        public String fullVersion;

        public String changelog;

        public int major;
        public int minor;
        public int patch;
        public int release;
        int alpha = 0;
        int beta = 0;
        boolean featureTest = false;

        public Version(String version, String version_number, String changelog) {
            this.fullVersion = version;
            this.changelog = changelog;
            String[] parts = version_number.split("-");
            int viveVersionIndex = 0;
            int releaseIndex = 1;
            // parts should be [vive version]-[release]_(a/b/test)
            String[] releaseParts = parts[releaseIndex].split("_");
            this.release = Integer.parseInt(releaseParts[0]);
            if (releaseParts.length > 1) {
                // prerelease
                if (releaseParts[1].matches("a\\d+.*")) {
                    this.alpha = Integer.parseInt(releaseParts[1].replaceAll("\\D+", ""));
                } else if (releaseParts[1].matches("b\\d+.*")) {
                    this.beta = Integer.parseInt(releaseParts[1].replaceAll("\\D+", ""));
                }
                // if the prerelease string is not just aXX or bXX it's a feature test as well and ranked slightly higher
                if (!releaseParts[1].replaceAll("^[ab]\\d+", "").isEmpty()) {
                    this.featureTest = true;
                }
            }

            String[] ints = parts[viveVersionIndex].split("\\.");
            this.major = Integer.parseInt(ints[0]);
            this.minor = Integer.parseInt(ints[1]);
            this.patch = Integer.parseInt(ints[2]);
        }

        @Override
        public int compareTo(Version o) {
            long result = this.compareNumber() - o.compareNumber();
            if (result < 0) {
                return 1;
            } else if (result == 0L) {
                return 0;
            }
            return -1;
        }

        public boolean isVersionType(UpdateType versionType) {
            switch (versionType) {
                case RELEASE:
                    return this.beta == 0 && this.alpha == 0 && !this.featureTest;
                case BETA:
                    return this.beta >= 0 && this.alpha == 0 && !this.featureTest;
                case ALPHA:
                    return this.alpha >= 0 && !this.featureTest;
                default:
                    return false;
            }
        }

        // two digits per segment, should be enough right?
        private long compareNumber() {
            // digit flag
            // major minor patch release full release beta alpha feature test
            // 00    00    00    00      0            00   00    0
            return (this.featureTest ? 1L : 0L) +
                this.alpha * 10L +
                this.beta * 1000L +
                (this.alpha + this.beta == 0 ? 10000L : 0L) +
                this.release * 1000000L +
                this.patch * 100000000L +
                this.minor * 10000000000L +
                this.major * 1000000000000L;
        }
    }
}
