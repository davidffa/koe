package moe.kyokobot.koe.internal.util;

import java.util.Arrays;

public class AudioPacket {
  private final byte[] message;
  private final byte flags;
  private final long ssrc;

  public AudioPacket(byte[] message, byte flags, long ssrc) {
    this.message = message;
    this.flags = flags;
    this.ssrc = ssrc;
  }

  public byte[] getOpusAudio() {
    int offset = 32; // crypto_secretbox_ZEROBYTES

    boolean hasExtension = (flags & 0b10000) != 0;
    byte cc = (byte) (flags & 0b1111);

    if (cc > 0) {
      offset += cc * 4;
      System.out.println("CC Offset: " + offset);
    }

    if (hasExtension) {
      int l = (message[offset + 2] & 0xff) << 8 | (message[offset + 3] & 0xff);
      offset += 4 + l * 4;
    }

    return Arrays.copyOfRange(message, offset, message.length);
  }

  public long getSsrc() {
    return ssrc;
  }
}
