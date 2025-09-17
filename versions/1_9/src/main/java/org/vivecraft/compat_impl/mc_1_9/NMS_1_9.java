package org.vivecraft.compat_impl.mc_1_9;

import org.vivecraft.accessors.EntityMapping;
import org.vivecraft.compat_impl.mc_1_8.NMS_1_8;
import org.vivecraft.util.reflection.ReflectionMethod;

public class NMS_1_9 extends NMS_1_8 {
    protected ReflectionMethod ServerPlayer_getServer;

    @Override
    protected void initServer() {
        this.ServerPlayer_getServer = ReflectionMethod.getMethod(EntityMapping.METHOD_GET_SERVER);
    }

    @Override
    public Object getServer(Object serverPlayer) {
        return this.ServerPlayer_getServer.invoke(serverPlayer);
    }
}
