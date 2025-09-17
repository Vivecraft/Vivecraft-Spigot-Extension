package org.vivecraft.compat_impl.mc_1_21;

import org.vivecraft.accessors.ServerboundUseItemPacketMapping;
import org.vivecraft.compat_impl.mc_1_20_6.NMS_1_20_6;
import org.vivecraft.util.reflection.ReflectionField;

public class NMS_1_21 extends NMS_1_20_6 {

    protected ReflectionField ServerboundUseItemPacket_xRot;
    protected ReflectionField ServerboundUseItemPacket_yRot;

    @Override
    protected void initAimFix() {
        super.initAimFix();
        this.ServerboundUseItemPacket_xRot = ReflectionField.getField(
            ServerboundUseItemPacketMapping.FIELD_X_ROT);
        this.ServerboundUseItemPacket_yRot = ReflectionField.getField(
            ServerboundUseItemPacketMapping.FIELD_Y_ROT);
    }

    @Override
    public void handlePacket(Object packet, Object packetListener, float xRot, float yRot) {
        if (this.ServerboundUseItemPacket.isInstance(packet)) {
            this.ServerboundUseItemPacket_xRot.set(packet, xRot);
            this.ServerboundUseItemPacket_yRot.set(packet, yRot);
        }
        this.Packet_handle.invoke(packet, packetListener);
    }
}
