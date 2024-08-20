package moe.kyokobot.koe.crypto;

import com.google.crypto.tink.aead.internal.InsecureNonceXChaCha20Poly1305;
import com.google.crypto.tink.aead.internal.Poly1305;
import io.netty.buffer.ByteBuf;

import java.nio.ByteBuffer;

public class XChaCha20Poly1305EncryptionMode implements EncryptionMode {
    private final byte[] extendedNonce = new byte[24];
    private final ByteBuffer c = ByteBuffer.allocate(1276 + Poly1305.MAC_TAG_SIZE_IN_BYTES);

    private final byte[] rtpHeader = new byte[12];
    private int seq = 0x80000000;

    private InsecureNonceXChaCha20Poly1305 cipher;

    @Override
    @SuppressWarnings("Duplicates")
    public boolean box(ByteBuf packet, int len, ByteBuf output, byte[] secretKey) {
        byte[] m = new byte[len];

        for (int i = 0; i < len; i++) {
            m[i] = packet.readByte();
        }

        int s = this.seq++;
        extendedNonce[0] = (byte) (s & 0xff);
        extendedNonce[1] = (byte) ((s >> 8) & 0xff);
        extendedNonce[2] = (byte) ((s >> 16) & 0xff);
        extendedNonce[3] = (byte) ((s >> 24) & 0xff);

        // RTP Header already written to the output buffer
        output.readBytes(rtpHeader);
        output.resetReaderIndex();

        try {
            if (cipher == null)
                cipher = new InsecureNonceXChaCha20Poly1305(secretKey);

            c.clear();
            c.limit(len + Poly1305.MAC_TAG_SIZE_IN_BYTES);
            cipher.encrypt(c, extendedNonce, m, rtpHeader);
        } catch (Exception e) {
            return false;
        }

        output.writeBytes(c.flip());
        output.writeIntLE(s);
        return true;
    }

    @Override
    public String getName() {
        return "aead_xchacha20_poly1305_rtpsize";
    }
}
