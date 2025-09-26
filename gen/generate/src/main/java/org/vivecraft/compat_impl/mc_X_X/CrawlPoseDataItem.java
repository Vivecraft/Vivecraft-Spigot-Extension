package org.vivecraft.compat_impl.mc_X_X;

import org.bukkit.entity.Player;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;

public class CrawlPoseDataItem extends net.minecraft.network.syncher.SynchedEntityData$DataItem {

    protected final Player player;

    public CrawlPoseDataItem(
        net.minecraft.network.syncher.EntityDataAccessor entityDataAccessor,
        net.minecraft.world.entity.Pose initialValue, Player player)
    {
        super(entityDataAccessor, initialValue);
        this.player = player;
    }

    @Override
    public void setValue(Object pose) {
        VivePlayer vivePlayer = ViveMain.getVivePlayer(this.player);
        if (vivePlayer != null && vivePlayer.isVR() && vivePlayer.crawling) {
            super.setValue(net.minecraft.world.entity.Pose.SWIMMING);
        } else {
            super.setValue(pose);
        }
    }
}
