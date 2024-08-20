package moe.kyokobot.koe.codec;

public class OpusCodec extends Codec {
    public static final byte PAYLOAD_TYPE = (byte) 120;
    public static final int FRAME_DURATION = 20;
    public static final OpusCodec INSTANCE = new OpusCodec();
    public static final byte[] SILENCE_FRAME = new byte[] {(byte)0xF8, (byte)0xFF, (byte)0xFE};
    public static final int MAX_FRAME_SIZE = 1276; // https://datatracker.ietf.org/doc/html/rfc6716#section-3.2.1

    public OpusCodec() {
        super("opus", PAYLOAD_TYPE, 1000);
    }
}
