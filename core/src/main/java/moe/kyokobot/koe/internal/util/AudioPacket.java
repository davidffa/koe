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

  public AudioPacket(byte[] message, int len, byte flags, int seq, long timestamp, long ssrc, int extensionLength, boolean useDirectBuffer) {
    this.flags = flags;
    this.seq = seq;
    this.timestamp = timestamp;
    this.ssrc = ssrc;
    this.receivedTimestamp = System.currentTimeMillis();

    this.extractOpus(message, len, extensionLength, useDirectBuffer);
  }

  private void extractOpus(byte[] msg, int len, int extensionLength, boolean useDirectBuffer) {
    int offset = 4 * extensionLength;

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
