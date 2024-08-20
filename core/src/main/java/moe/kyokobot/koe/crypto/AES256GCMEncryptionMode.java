package moe.kyokobot.koe.crypto;

import io.netty.buffer.ByteBuf;
import moe.kyokobot.koe.codec.OpusCodec;
import moe.kyokobot.koe.internal.util.AudioPacket;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;

public class AES256GCMEncryptionMode implements EncryptionMode {
    private static final int GCM_TAG_LENGTH = 16;

    private final byte[] extendedNonce = new byte[12];
    private final byte[] m = new byte[OpusCodec.MAX_FRAME_SIZE];
    private final byte[] c = new byte[OpusCodec.MAX_FRAME_SIZE + GCM_TAG_LENGTH];
    private final byte[] rtpHeader = new byte[12];

    private final byte[] decryptNonce = new byte[12];
    private final byte[] m2 = new byte[OpusCodec.MAX_FRAME_SIZE];
    private final byte[] c2 = new byte[OpusCodec.MAX_FRAME_SIZE + GCM_TAG_LENGTH];
    private final byte[] receivedRtpHeader = new byte[12 + 15*4 + 4]; // max 15 ccrc + 4 bytes for header extension

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
    public boolean encrypt(ByteBuf packet, int len, ByteBuf output, byte[] secretKey) {
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
    @SuppressWarnings("Duplicates")
    public AudioPacket decrypt(ByteBuf packet, byte[] secretKey, boolean useDirectBuffer) {
        byte flags = packet.readByte();
        packet.readerIndex(2); // Skip payload_type
        int seq = packet.readUnsignedShort();
        long timestamp = packet.readUnsignedInt();
        long ssrc = packet.readUnsignedInt();

        boolean hasExtension = (flags & 0b10000) != 0;
        int cc = flags & 0b1111;

        int rtpHeaderLength = 12 + 4*cc;
        int extensionLength = 0;

        if (hasExtension) {
            packet.readerIndex(packet.readerIndex() + 2);
            extensionLength = packet.readByte() << 8 | packet.readByte();
            rtpHeaderLength += 4;
        }

        packet.resetReaderIndex();
        packet.readBytes(receivedRtpHeader, 0, rtpHeaderLength);

        int len = packet.readableBytes() - 4;
        packet.readBytes(c2, 0, len);

        packet.readBytes(decryptNonce, 0, 4);

        GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, decryptNonce);
        SecretKeySpec keySpec = new SecretKeySpec(secretKey, "AES");

        try {
            cipher.init(Cipher.DECRYPT_MODE, keySpec, spec);
            cipher.updateAAD(receivedRtpHeader, 0, rtpHeaderLength);
            cipher.doFinal(c2, 0, len, m2, 0);
        } catch (Exception e) {
            return null;
        }

        return new AudioPacket(m2, len - GCM_TAG_LENGTH, flags, seq, timestamp, ssrc, extensionLength, useDirectBuffer);
    }

    @Override
    public String getName() {
        return "aead_aes256_gcm_rtpsize";
    }
}
