package org.vivecraft.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Vector3fc;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;
import org.vivecraft.compat.Platform;
import org.vivecraft.data.PlayerState;
import org.vivecraft.debug.Debug;

import java.util.UUID;

public class AimFixHandler extends ChannelInboundHandlerAdapter {
    private final Object netManager;
    private final UUID palyerId;

    public AimFixHandler(Player player, Object netManager) {
        this.netManager = netManager;
        this.palyerId = player.getUniqueId();
        ViveMain.NMS.getChannel(netManager).pipeline().addBefore("packet_handler", "vr_aim_fix", this);
    }

    /**
     * checks if the {@code msg}  uses the players aim, and changes it to the right position before handling
     *
     * @param ctx context when not handling the message
     * @param msg Packet to handle
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        Object listener = ViveMain.NMS.getPacketListener(this.netManager);

        if (!ViveMain.VIVE_PLAYERS.containsKey(this.palyerId) || !ViveMain.VIVE_PLAYERS.get(this.palyerId).isVR() ||
            !ViveMain.NMS.needsAimfixHandling(msg))
        {
            // we don't need to handle this packet, just defer to the next handler in the pipeline
            ctx.fireChannelRead(msg);
            return;
        }

        Object player = ViveMain.NMS.getPlayer(listener);

        Runnable task = () -> {
            // Save all the current orientation data
            PlayerState oldState = ViveMain.NMS.getPlayerState(player);

            VivePlayer vivePlayer = ViveMain.VIVE_PLAYERS.get(this.palyerId);

            float xRot = oldState.xRot;
            float yRot = oldState.yRot;
            Vector aimPos = null;
            // Check again in case of race condition
            if (vivePlayer != null && vivePlayer.isVR()) {
                // use the aim the client sent
                aimPos = vivePlayer.getAimPos(false);
                Vector3fc dir = vivePlayer.getAimDir(false);

                // Inject our custom orientation data
                xRot = (float) Math.toDegrees(Math.asin(-dir.y()));
                yRot = (float) Math.toDegrees(Math.atan2(-dir.x(), dir.z()));
                ViveMain.NMS.setPlayerState(player, aimPos, xRot, yRot);

                // Set up offset to fix relative positions
                vivePlayer.offset.setX(oldState.x - aimPos.getX());
                vivePlayer.offset.setY(oldState.y - aimPos.getY());
                vivePlayer.offset.setZ(oldState.z - aimPos.getZ());
                Debug.log("AimFix: pos: %s, dir: %s", aimPos, dir);
            }

            // Call the packet handler directly
            // This is several implementation details that we have to replicate
            try {
                if (ViveMain.NMS.isConnectionConnected(this.netManager)) {
                    try {
                        ViveMain.NMS.handlePacket(player, msg, listener, xRot, yRot);
                    } catch (Exception ignored) {
                        // Apparently might get thrown and can be ignored
                    }
                }
            } finally {
                // Vanilla uses SimpleChannelInboundHandler, which automatically releases
                // by default, so we're expected to release the packet once we're done.
                ReferenceCountUtil.release(msg);
            }

            // Restore the original orientation data
            if (ViveMain.NMS.restorePlayerState(player, oldState, aimPos)) {
                Debug.log("AimFix: AimFix moved Player");
            }

            // Reset offset
            if (vivePlayer != null) {
                vivePlayer.offset.zero();
            }
        };
        if (Platform.FOLIA) {
            // with folia we need to schedule it for the player thread
            Platform.getInstance().getScheduler().runEntity(Bukkit.getPlayer(this.palyerId), task);
        } else {
            // without folia try to run it immediately
            ViveMain.NMS.runOnMainThread(ViveMain.NMS.getServer(player), task);
        }
    }
}
