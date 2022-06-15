package moe.kyokobot.koe.internal.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioPacket {
  private final byte[] message;
  private final byte flags;
  private final long ssrc;
  private final long receivedTimestamp;
  private final boolean useDirectBuffer;

  public AudioPacket(byte[] message, byte flags, long ssrc, boolean useDirectBuffer) {
    this.message = message;
    this.flags = flags;
    this.ssrc = ssrc;
    this.receivedTimestamp = System.currentTimeMillis();
    this.useDirectBuffer = useDirectBuffer;
  }

  public ByteBuffer getOpusAudio() {
    int offset = 32; // crypto_secretbox_ZEROBYTES

    boolean hasExtension = (flags & 0b10000) != 0;
    byte cc = (byte) (flags & 0b1111);

    if (cc > 0) {
      offset += cc * 4;
    }

    if (hasExtension) {
      int l = (message[offset + 2] & 0xff) << 8 | (message[offset + 3] & 0xff);
      offset += 4 + l * 4;
    }

    if (useDirectBuffer) {
      return ByteBuffer.allocateDirect(message.length - offset)
              .order(ByteOrder.nativeOrder())
              .put(message, offset, message.length - offset);
    }

    return ByteBuffer.wrap(message, offset, message.length - offset);
  }

  public long getSsrc() {
    return ssrc;
  }

  public long getReceivedTimestamp() {
    return receivedTimestamp;
  }
}
