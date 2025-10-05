package org.vivecraft.compat_impl.mc_1_21_9;

import org.vivecraft.accessors.PacketMapping;
import org.vivecraft.accessors.ServerLevelMapping;
import org.vivecraft.compat_impl.mc_1_21_5.NMS_1_21_5;
import org.vivecraft.util.reflection.ClassGetter;
import org.vivecraft.util.reflection.ReflectionConstructor;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_21_9 extends NMS_1_21_5 {

    ReflectionMethod Packet_handle;
    ReflectionConstructor WrappedPacket_Constructor;

    @Override
    protected void initAimFix() {
        super.initAimFix();
        this.WrappedPacket_Constructor = ReflectionConstructor.getCompat("WrappedPacket", Runnable.class,
            ClassGetter.getClass(true, ServerLevelMapping.MAPPING));
        this.Packet_handle = ReflectionMethod.getMethod(PacketMapping.METHOD_HANDLE);
    }

    @Override
    protected void initServer() {}

    @Override
    public Object getServer(Object serverPlayer) {
        return this.ServerLevel_getServer.invoke(this.Entity_getLevel.invoke(serverPlayer));
    }

    @Override
    public void handlePacketTask(Object packetListener, Runnable task, Object player) {
        this.Packet_handle.invoke(this.WrappedPacket_Constructor.newInstance(task, getLevel(player)), packetListener);
    }

    @Override
    protected void startRiding(Object vehicle, Object passanger, boolean force) {
        this.Entity_startRiding.invoke(passanger, vehicle, force, false);
    }
}
