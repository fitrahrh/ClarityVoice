package me.harukiyu.clarityvoice.audio;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

public final class AudioPayloadHelper {

    public static final byte PACKET_TYPE_AUDIO_BROADCAST = 0x01;
    public static final byte PACKET_TYPE_CLIENT_MIC = 0x02;
    public static final byte PACKET_TYPE_GROUP_AUDIO = 0x03;
    public static final int SAMPLES_PER_FRAME = 960;
    public static final int BYTES_PER_FRAME = SAMPLES_PER_FRAME * 2; // 1920 bytes

    private AudioPayloadHelper() {}

    /**
     * Builds a binary audio packet to be sent to Web clients.
     * Format:
     * - 1 byte magic type (0x01)
     * - 16 bytes UUID (8 bytes MSB, 8 bytes LSB - Big Endian)
     * - 24 bytes coordinates (X, Y, Z - Little Endian doubles)
     * - 1920 bytes 16-bit PCM (960 shorts - Little Endian)
     */
    public static byte[] createAudioBroadcastPacket(UUID senderUuid, double x, double y, double z, short[] pcmSamples) {
        int capacity = 1 + 16 + 24 + (pcmSamples.length * 2);
        ByteBuffer buffer = ByteBuffer.allocate(capacity);

        // Packet type
        buffer.put(PACKET_TYPE_AUDIO_BROADCAST);

        // UUID
        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(senderUuid.getMostSignificantBits());
        buffer.putLong(senderUuid.getLeastSignificantBits());

        // Coordinates & Audio PCM in Little Endian (native for Web Audio API / DataView)
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putDouble(x);
        buffer.putDouble(y);
        buffer.putDouble(z);

        for (short sample : pcmSamples) {
            buffer.putShort(sample);
        }

        return buffer.array();
    }

    /**
     * Builds a binary group audio packet to be sent to Web clients in the same Voice Group.
     * Format:
     * - 1 byte magic type (0x03)
     * - 16 bytes sender UUID (8 bytes MSB, 8 bytes LSB - Big Endian)
     * - 16 bytes group UUID (8 bytes MSB, 8 bytes LSB - Big Endian)
     * - 1920 bytes 16-bit PCM (960 shorts - Little Endian)
     */
    public static byte[] createGroupAudioPacket(UUID senderUuid, UUID groupId, short[] pcmSamples) {
        int capacity = 1 + 16 + 16 + (pcmSamples.length * 2);
        ByteBuffer buffer = ByteBuffer.allocate(capacity);

        buffer.put(PACKET_TYPE_GROUP_AUDIO);

        buffer.order(ByteOrder.BIG_ENDIAN);
        buffer.putLong(senderUuid.getMostSignificantBits());
        buffer.putLong(senderUuid.getLeastSignificantBits());
        buffer.putLong(groupId != null ? groupId.getMostSignificantBits() : 0L);
        buffer.putLong(groupId != null ? groupId.getLeastSignificantBits() : 0L);

        buffer.order(ByteOrder.LITTLE_ENDIAN);
        for (short sample : pcmSamples) {
            buffer.putShort(sample);
        }

        return buffer.array();
    }

    /**
     * Extracts 16-bit PCM short[] samples from binary Web mic audio payload.
     * Supports both framed [0x02 + 1920 bytes] and raw [1920 bytes].
     */
    public static short[] extractPcmSamples(byte[] data) {
        if (data == null) return null;

        int offset = 0;
        int length = data.length;

        if (length == BYTES_PER_FRAME + 1 && data[0] == PACKET_TYPE_CLIENT_MIC) {
            offset = 1;
            length = BYTES_PER_FRAME;
        } else if (length != BYTES_PER_FRAME) {
            return null;
        }

        short[] samples = new short[SAMPLES_PER_FRAME];
        ByteBuffer buffer = ByteBuffer.wrap(data, offset, length);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.asShortBuffer().get(samples);

        return samples;
    }
}
