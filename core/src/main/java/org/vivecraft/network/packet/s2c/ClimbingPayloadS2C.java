package org.vivecraft.network.packet.s2c;

import org.jetbrains.annotations.Nullable;
import org.vivecraft.config.enums.ClimbeyBlockmode;
import org.vivecraft.network.packet.PayloadIdentifier;
import org.vivecraft.util.BufferUtils;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * packet that holds if the server allows climbing, and optionally a list of blocks that are allowed or blocked
 *
 */
public final class ClimbingPayloadS2C implements VivecraftPayloadS2C {
    public final boolean allowed;
    public final ClimbeyBlockmode blockmode;
    @Nullable
    public final List<String> blocks;

    /**
     * @param allowed   if climbing is enabled
     * @param blockmode id of the block mode. 0: DISABLED, 1: WHITELIST, 2: BLACKLIST
     * @param blocks    list of blocks, can be {@code null}
     */
    public ClimbingPayloadS2C(boolean allowed, ClimbeyBlockmode blockmode, @Nullable List<String> blocks) {
        this.allowed = allowed;
        this.blockmode = blockmode;
        this.blocks = blocks;
    }

    @Override
    public PayloadIdentifier payloadId() {
        return PayloadIdentifier.CLIMBING;
    }

    @Override
    public void write(DataOutputStream buffer) throws IOException {
        buffer.writeByte(payloadId().ordinal());
        buffer.writeBoolean(this.allowed);
        buffer.writeByte(this.blockmode.ordinal());
        if (this.blocks != null) {
            for (String block : this.blocks) {
                BufferUtils.writeMCString(buffer, block);
            }
        }
    }
}
