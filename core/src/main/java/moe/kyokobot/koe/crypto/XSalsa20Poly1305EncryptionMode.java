package moe.kyokobot.koe.crypto;

import io.netty.buffer.ByteBuf;
import moe.kyokobot.koe.internal.crypto.TweetNaclFastInstanced;
import moe.kyokobot.koe.internal.util.AudioPacket;

import java.util.Arrays;
import java.util.Base64;

public class XSalsa20Poly1305EncryptionMode implements EncryptionMode {
    private final byte[] extendedNonce = new byte[24];
    private final byte[] m = new byte[1276 + ZERO_BYTES_LENGTH];
    private final byte[] c = new byte[1276 + ZERO_BYTES_LENGTH];
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
    public AudioPacket open(ByteBuf packet, byte[] secretKey) {
        Arrays.fill(c2, (byte) 0);

        byte flags = packet.getByte(0);
        long ssrc = packet.getUnsignedInt(8);

        byte[] nonce = new byte[24];

        packet.readBytes(nonce, 0, 12);

        int len = packet.readableBytes();
        packet.readBytes(c2, 16, len);

        byte[] message = new byte[len + 16];

        if (0 == nacl.cryptoSecretboxXSalsa20Poly1305Open(message, c2, len + 16, nonce, secretKey)) {
            return new AudioPacket(message, flags, ssrc);
        }

        return null;
    }

    @Override
    public String getName() {
        return "xsalsa20_poly1305";
    }
}
