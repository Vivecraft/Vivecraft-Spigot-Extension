package org.vivecraft.compat_impl.mc_26_1;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.PacketUtils;
import net.minecraft.server.RunningOnDifferentThreadException;
import net.minecraft.server.level.ServerLevel;

public class WrappedPacket implements Packet<PacketListener> {

    private final Runnable runnable;
    private final ServerLevel level;

    public WrappedPacket(Runnable runnable, ServerLevel level) {
        this.runnable = runnable;
        this.level = level;
    }

    @Override
    public PacketType<? extends Packet<PacketListener>> type() {
        return null;
    }

    @Override
    public void handle(PacketListener handler) {
        try {
            PacketUtils.ensureRunningOnSameThread(this, handler, this.level);
            this.runnable.run();
        } catch (RunningOnDifferentThreadException e) {}
    }
}
