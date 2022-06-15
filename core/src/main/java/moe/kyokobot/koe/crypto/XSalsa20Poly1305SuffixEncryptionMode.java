package moe.kyokobot.koe.crypto;

import io.netty.buffer.ByteBuf;
import moe.kyokobot.koe.internal.crypto.TweetNaclFastInstanced;
import moe.kyokobot.koe.internal.util.AudioPacket;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class XSalsa20Poly1305SuffixEncryptionMode implements EncryptionMode {
    private final byte[] extendedNonce = new byte[24];
    private final byte[] m = new byte[984];
    private final byte[] c = new byte[984];
    private final byte[] openNonce = new byte[24];
    private final byte[] c2 = new byte[984];

    private final TweetNaclFastInstanced nacl = new TweetNaclFastInstanced();

    @Override
    @SuppressWarnings("Duplicates")
    public boolean box(ByteBuf packet, int len, ByteBuf output, byte[] secretKey) {
        for (int i = 0; i < c.length; i++) {
            m[i] = 0;
            c[i] = 0;
        }

        for (int i = 0; i < len; i++) {
            m[i + 32] = packet.readByte();
        }

        ThreadLocalRandom.current().nextBytes(extendedNonce);

        if (0 == nacl.cryptoSecretboxXSalsa20Poly1305(c, m, len + 32, extendedNonce, secretKey)) {
            for (int i = 0; i < (len + 16); i++) {
                output.writeByte(c[i + 16]);
            }
            output.writeBytes(extendedNonce);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public AudioPacket open(ByteBuf packet, byte[] secretKey, boolean useDirectBuffer) {
        Arrays.fill(c2, (byte) 0);

        byte flags = packet.readByte();
        packet.readerIndex(8); // Skip unused RTP Header params
        long ssrc = packet.readUnsignedInt();

        int len = packet.readableBytes() - 24;
        packet.readBytes(c2, 16, len);

        packet.readBytes(openNonce, 0, 24);

        byte[] message = new byte[len + 16];

        if (0 == nacl.cryptoSecretboxXSalsa20Poly1305Open(message, c2, len + 16, openNonce, secretKey)) {
            return new AudioPacket(message, flags, ssrc, useDirectBuffer);
        }

        return null;
    }

    @Override
    public String getName() {
        return "xsalsa20_poly1305_suffix";
    }
}
