package moe.kyokobot.koe.crypto;

import io.netty.buffer.ByteBuf;
import moe.kyokobot.koe.internal.util.AudioPacket;

public class PlainEncryptionMode implements EncryptionMode {
    @Override
    public boolean box(ByteBuf opus, int start, ByteBuf output, byte[] secretKey) {
        opus.readerIndex(start);
        output.writeBytes(opus);
        return true;
    }

    @Override
    public AudioPacket open(ByteBuf packet, byte[] secretKey, boolean useDirectBuffer) {
        byte flags = packet.readByte();
        packet.readerIndex(8);
        long ssrc = packet.readUnsignedInt();

        int len = packet.readableBytes();
        byte[] output = new byte[len];
        packet.readBytes(output, 0, len);

        return new AudioPacket(output, flags, ssrc, useDirectBuffer);
    }

    @Override
    public String getName() {
        return "plain";
    }
}
