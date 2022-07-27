package moe.kyokobot.koe.internal.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioPacket {
  private ByteBuffer opus;
  private final byte flags;
  private final int seq;
  private final long timestamp;
  private final long ssrc;
  private final long receivedTimestamp;

  public AudioPacket(byte[] message, int len, byte flags, int seq, long timestamp, long ssrc, boolean useDirectBuffer) {
    this.flags = flags;
    this.seq = seq;
    this.timestamp = timestamp;
    this.ssrc = ssrc;
    this.receivedTimestamp = System.currentTimeMillis();

    this.extractOpus(message, len, useDirectBuffer);
  }

  private void extractOpus(byte[] msg, int len, boolean useDirectBuffer) {
    int offset = 32; // crypto_secretbox_ZEROBYTES

    boolean hasExtension = (flags & 0b10000) != 0;
    byte cc = (byte) (flags & 0b1111);

    if (cc > 0) {
      offset += cc * 4;
    }

    if (hasExtension) {
      int l = (msg[offset + 2] & 0xff) << 8 | (msg[offset + 3] & 0xff);
      offset += 4 + l * 4;
    }

    if (useDirectBuffer) {
      opus = ByteBuffer.allocateDirect(msg.length - offset)
              .order(ByteOrder.nativeOrder())
              .put(msg, offset, len - offset)
              .flip();

      return;
    }

    opus = ByteBuffer.wrap(msg, offset, msg.length - offset);
  }

  public ByteBuffer getOpusAudio() {
    return opus;
  }

  public byte getFlags() {
    return flags;
  }

  public int getSeq() {
    return seq;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public long getSsrc() {
    return ssrc;
  }

  public long getReceivedTimestamp() {
    return receivedTimestamp;
  }
}
