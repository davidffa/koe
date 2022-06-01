package moe.kyokobot.koe.handler;

import moe.kyokobot.koe.internal.util.AudioPacket;

public interface AudioReceiveHandler {
  default void handleAudio(AudioPacket packet) {

  }
}
