package org.vivecraft.pluginsupport;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;

public class VivecraftPAPIExpansion extends PlaceholderExpansion {

    public VivecraftPAPIExpansion() {}

    @Override
    public String getAuthor() {
        // use authors from plugin yml
        return String.join(", ", ViveMain.INSTANCE.getDescription().getAuthors());
    }

    @Override
    public String getIdentifier() {
        return "vivecraft";
    }

    @Override
    public String getVersion() {
        // use version from plugin yml
        return ViveMain.INSTANCE.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        // registration persists on plugin reload
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        if (player != null) {
            VivePlayer vivePlayer = ViveMain.getVivePlayer(player.getUniqueId());
            if ("mode".equals(params)) {
                if (vivePlayer != null) {
                    if (!vivePlayer.isVR()) {
                        return ViveMain.CONFIG.papiModeNonVR.get();
                    } else if (vivePlayer.isSeated()) {
                        return ViveMain.CONFIG.papiModeSeatedVR.get();
                    } else {
                        return ViveMain.CONFIG.papiModeVR.get();
                    }
                } else {
                    return ViveMain.CONFIG.papiModeVanilla.get();
                }
            }
        }

        // invalid placeholder
        return null;
    }
}
