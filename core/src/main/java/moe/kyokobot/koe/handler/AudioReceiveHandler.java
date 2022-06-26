package moe.kyokobot.koe.handler;

import moe.kyokobot.koe.internal.util.AudioPacket;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public interface AudioReceiveHandler {
  /**
   * Whether to convert the opus frames into a direct buffer or not
   */
  default boolean useDirectBuffer() {
    return true;
  }

  /**
   * Allows to choose the users that will be recorded
   * @return A set of user ids or null to record all users
   */
  @Nullable
  default Set<String> users() {
    return null;
  }

  /**
   * Called when received and decoded an audio packet
   * @param packet The audio packet
   */
  void handleAudio(AudioPacket packet);
}
