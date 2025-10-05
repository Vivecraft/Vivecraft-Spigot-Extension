package org.vivecraft.compat_impl.mc_X_X;

public class WrappedPacket implements net.minecraft.network.protocol.Packet {

    private final Runnable runnable;
    private final net.minecraft.server.level.ServerLevel level;

    public WrappedPacket(Runnable runnable, net.minecraft.server.level.ServerLevel level) {
        this.runnable = runnable;
        this.level = level;
    }

    @Override
    public net.minecraft.network.protocol.PacketType type() {
        return null;
    }

    @Override
    public void handle(net.minecraft.network.PacketListener handler) {
        try {
            net.minecraft.network.protocol.PacketUtils.ensureRunningOnSameThread(this, handler, this.level);
            this.runnable.run();
        } catch (net.minecraft.server.RunningOnDifferentThreadException e) {}
    }
}
