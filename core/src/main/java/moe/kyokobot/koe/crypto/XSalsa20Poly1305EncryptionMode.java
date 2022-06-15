package moe.kyokobot.koe.crypto;

import io.netty.buffer.ByteBuf;
import moe.kyokobot.koe.internal.crypto.TweetNaclFastInstanced;
import moe.kyokobot.koe.internal.util.AudioPacket;

public class XSalsa20Poly1305EncryptionMode implements EncryptionMode {
    private final byte[] extendedNonce = new byte[24];
    private final byte[] m = new byte[1276 + ZERO_BYTES_LENGTH];
    private final byte[] c = new byte[1276 + ZERO_BYTES_LENGTH];
    private final byte[] c2 = new byte[984];
    private final byte[] m2 = new byte[976]; // FRAME_SIZE + 16 reserved for MAC_BYTES
    private final byte[] openNonce = new byte[24];
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

        output.getBytes(0, extendedNonce, 0, 12);

        if (0 == nacl.cryptoSecretboxXSalsa20Poly1305(c, m, len + 32, extendedNonce, secretKey)) {
            for (int i = 0; i < (len + 16); i++) {
                output.writeByte(c[i + 16]);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public AudioPacket open(ByteBuf packet, byte[] secretKey, boolean useDirectBuffer) {
        int i;
        for (i = 0; i < m2.length; i++) {
            m2[i] = 0;
            c2[i] = 0;
        }

        for (; i < c2.length; i++) {
            c2[i] = 0;
        }

        byte flags = packet.getByte(0);
        long ssrc = packet.getUnsignedInt(8);

        packet.readBytes(openNonce, 0, 12);

        int len = packet.readableBytes();
        packet.readBytes(c2, 16, len);

        if (0 == nacl.cryptoSecretboxXSalsa20Poly1305Open(m2, c2, len + 16, openNonce, secretKey)) {
            return new AudioPacket(m2, len + 16, flags, ssrc, useDirectBuffer);
        }

        return null;
    }

    @Override
    public String getName() {
        return "xsalsa20_poly1305";
    }
}
