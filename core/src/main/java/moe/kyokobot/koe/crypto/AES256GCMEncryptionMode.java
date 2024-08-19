package moe.kyokobot.koe.crypto;

import io.netty.buffer.ByteBuf;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

public class AES256GCMEncryptionMode implements EncryptionMode {
    private static final int GCM_TAG_LENGTH = 16;

    private final byte[] extendedNonce = new byte[12];
    private final byte[] m = new byte[1276];
    private final byte[] c = new byte[1276 + GCM_TAG_LENGTH];
    private final byte[] rtpHeader = new byte[12];
    private int seq = 0x80000000;

    private final Cipher cipher;

    public AES256GCMEncryptionMode() {
        try {
            cipher = Cipher.getInstance("AES/GCM/NoPadding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @SuppressWarnings("Duplicates")
    public boolean box(ByteBuf packet, int len, ByteBuf output, byte[] secretKey) {
        for (int i = 0; i < len; i++) {
            m[i] = packet.readByte();
        }

        int s = this.seq++;
        extendedNonce[0] = (byte) (s & 0xff);
        extendedNonce[1] = (byte) ((s >> 8) & 0xff);
        extendedNonce[2] = (byte) ((s >> 16) & 0xff);
        extendedNonce[3] = (byte) ((s >> 24) & 0xff);

        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, extendedNonce);
        SecretKeySpec keySpec = new SecretKeySpec(secretKey, "AES");

        // RTP Header already written to the output buffer
        output.readBytes(rtpHeader);
        output.resetReaderIndex();

        try {
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, spec);
            cipher.updateAAD(rtpHeader, 0, 12);
            cipher.doFinal(m, 0, len, c, 0);
        } catch (Exception e) {
            return false;
        }

        for (int i = 0; i < len + GCM_TAG_LENGTH; i++) {
            output.writeByte(c[i]);
        }

        output.writeIntLE(s);
        return true;
    }

    @Override
    public String getName() {
        return "aead_aes256_gcm_rtpsize";
    }
}
