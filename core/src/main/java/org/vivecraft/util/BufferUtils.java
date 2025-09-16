package org.vivecraft.util;

import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.vivecraft.ViveMain;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

public class BufferUtils {
    private static final int MAX_STRING_LENGTH = 32767;
    private static final int MAX_VARINT_SIZE = 5;
    private static final int DATA_BITS_MASK = 127;
    private static final int CONTINUATION_BIT_MASK = 128;
    private static final int DATA_BITS_PER_BYTE = 7;

    /**
     * writes a String to the given ByteBuffer, that can be read by mcs friendly bytebuffer
     *
     * @param buffer Buffer to write to
     * @param s      String to write
     */
    public static void writeMCString(DataOutputStream buffer, String s) throws IOException {
        if (s.length() > MAX_STRING_LENGTH) {
            ViveMain.LOGGER.severe("String too big (was " + s.length() + " characters, max " + MAX_STRING_LENGTH + ")");
        } else {
            byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
            writeVarInt(buffer, bytes.length);
            buffer.write(bytes);
        }
    }

    private static void writeVarInt(DataOutputStream buffer, int size) throws IOException {
        while ((size & -128) != 0) {
            buffer.writeByte(size & DATA_BITS_MASK | CONTINUATION_BIT_MASK);
            size >>>= DATA_BITS_PER_BYTE;
        }
        buffer.writeByte(size);
    }

    public static void writeUUID(DataOutputStream buffer, UUID uuid) throws IOException {
        buffer.writeLong(uuid.getMostSignificantBits());
        buffer.writeLong(uuid.getLeastSignificantBits());
    }

    public static void writeVector3f(DataOutputStream buffer, Vector3fc vector) throws IOException {
        buffer.writeFloat(vector.x());
        buffer.writeFloat(vector.y());
        buffer.writeFloat(vector.z());
    }

    public static Vector3f readVector3f(DataInputStream buffer) throws IOException {
        return new Vector3f(
            buffer.readFloat(),
            buffer.readFloat(),
            buffer.readFloat());
    }

    public static void writeQuat(DataOutputStream buffer, Quaternionfc quat) throws IOException {
        buffer.writeFloat(quat.w());
        buffer.writeFloat(quat.x());
        buffer.writeFloat(quat.y());
        buffer.writeFloat(quat.z());
    }

    public static Quaternionf readQuat(DataInputStream buffer) throws IOException {
        float w = buffer.readFloat();
        return new Quaternionf(
            buffer.readFloat(),
            buffer.readFloat(),
            buffer.readFloat(),
            w);
    }
}
