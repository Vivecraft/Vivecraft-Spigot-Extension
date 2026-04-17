package org.vivecraft.compat_impl.mc_26_1;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Pose;
import org.bukkit.entity.Player;
import org.vivecraft.ViveMain;
import org.vivecraft.VivePlayer;

public class CrawlPoseDataItem extends SynchedEntityData.DataItem<Pose> {

    protected final Player bukkitPlayer;

    public CrawlPoseDataItem(EntityDataAccessor<Pose> entityDataAccessor, Pose initialValue, Player bukkitPlayer) {
        super(entityDataAccessor, initialValue);
        this.bukkitPlayer = bukkitPlayer;
    }

    @Override
    public void setValue(Pose pose) {
        VivePlayer vivePlayer = ViveMain.getVivePlayer(this.bukkitPlayer);
        if (vivePlayer != null && vivePlayer.isVR() && vivePlayer.crawling) {
            super.setValue(net.minecraft.world.entity.Pose.SWIMMING);
        } else {
            super.setValue(pose);
        }
    }
}
