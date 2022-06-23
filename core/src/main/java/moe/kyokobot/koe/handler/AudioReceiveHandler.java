package moe.kyokobot.koe.handler;

import moe.kyokobot.koe.internal.util.AudioPacket;

public interface AudioReceiveHandler {
  default boolean useDirectBuffer() {
    return true;
  }

  default void handleAudio(AudioPacket packet) {

  }
}
