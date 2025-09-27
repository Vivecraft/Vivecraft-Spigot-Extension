package org.vivecraft.util;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.metadata.LazyMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;
import org.joml.Quaternionfc;
import org.joml.Vector3fc;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.api.data.VRBodyPart;

import java.util.concurrent.Callable;

public class MetadataHelper {
    public static void updateMetadata(VivePlayer vivePlayer) {
        updateBodyPart(vivePlayer, VRBodyPart.HEAD, "head");
        updateBodyPart(vivePlayer, VRBodyPart.MAIN_HAND, "righthand");
        updateBodyPart(vivePlayer, VRBodyPart.OFF_HAND, "lefthand");

        addOrInvalidateKey(vivePlayer, "seated", vivePlayer::isSeated);
        addOrInvalidateKey(vivePlayer, "height", () -> vivePlayer.heightScale);
        addOrInvalidateKey(vivePlayer, "activehand", () -> {
            switch (vivePlayer.activeBodyPart) {
                case MAIN_HAND:
                    return "right";
                case OFF_HAND:
                    return "left";
                default:
                    return vivePlayer.activeBodyPart.toString();
            }
        });
    }

    public static void cleanupMetadata(Player player) {
        player.removeMetadata("head.pos", ViveMain.INSTANCE);
        player.removeMetadata("head.aim", ViveMain.INSTANCE);
        player.removeMetadata("head.dir", ViveMain.INSTANCE);
        player.removeMetadata("head.rot", ViveMain.INSTANCE);
        player.removeMetadata("righthand.pos", ViveMain.INSTANCE);
        player.removeMetadata("righthand.aim", ViveMain.INSTANCE);
        player.removeMetadata("righthand.dir", ViveMain.INSTANCE);
        player.removeMetadata("righthand.rot", ViveMain.INSTANCE);
        player.removeMetadata("lefthand.pos", ViveMain.INSTANCE);
        player.removeMetadata("lefthand.aim", ViveMain.INSTANCE);
        player.removeMetadata("lefthand.dir", ViveMain.INSTANCE);
        player.removeMetadata("lefthand.rot", ViveMain.INSTANCE);
        player.removeMetadata("seated", ViveMain.INSTANCE);
        player.removeMetadata("height", ViveMain.INSTANCE);
        player.removeMetadata("activehand", ViveMain.INSTANCE);
    }

    private static void updateBodyPart(VivePlayer vivePlayer, VRBodyPart bodyPart, String key) {
        addOrInvalidateKey(vivePlayer, key + ".pos", () -> getLocation(vivePlayer, bodyPart));
        addOrInvalidateKey(vivePlayer, key + ".dir", () -> MathUtils.toBukkitVec(vivePlayer.getBodyPartDir(bodyPart)));
        addOrInvalidateKey(vivePlayer, key + ".rot", () -> {
            Quaternionfc quat = vivePlayer.getBodyPartOrientation(VRBodyPart.HEAD);
            return new float[]{quat.w(), quat.x(), quat.y(), quat.z()};
        });

        // this is just here for legacy support, if anyone really used those
        addOrInvalidateKey(vivePlayer, key + ".aim", () -> getVec3(vivePlayer.getBodyPartDir(bodyPart)));
    }

    private static void addOrInvalidateKey(VivePlayer vivePlayer, String key, Callable<Object> lazyValue) {
        if (!vivePlayer.player.hasMetadata(key)) {
            vivePlayer.player.setMetadata(key, new LazyMetadataValue(ViveMain.INSTANCE, lazyValue));
        } else {
            MetadataValue value = vivePlayer.player.getMetadata(key).stream()
                .filter(v -> v.getOwningPlugin() == ViveMain.INSTANCE).findFirst()
                .orElseThrow(() -> new RuntimeException("someone messed with our metadata"));
            value.invalidate();
        }
    }

    private static Location getLocation(VivePlayer vivePlayer, VRBodyPart bodyPart) {
        Vector pos = vivePlayer.getBodyPartPos(bodyPart);
        Location loc = new Location(vivePlayer.player.getWorld(), pos.getX(), pos.getY(), pos.getZ());
        loc.setDirection(MathUtils.toBukkitVec(vivePlayer.getBodyPartDir(bodyPart)));
        return loc;
    }

    private static Object getVec3(Vector3fc dir) {
        return ViveMain.NMS.newVec3(dir.x(), dir.y(), dir.z());
    }
}
