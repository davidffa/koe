package moe.kyokobot.koe.internal.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import moe.kyokobot.koe.MediaConnection;
import moe.kyokobot.koe.codec.OpusCodec;
import moe.kyokobot.koe.internal.util.AudioPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AudioReceiverHandler extends SimpleChannelInboundHandler<DatagramPacket> {
  private static final Logger logger = LoggerFactory.getLogger(AudioReceiverHandler.class);
  private final DiscordUDPConnection udpConnection;
  private final MediaConnection connection;

  private final Map<String, Short> silenceCount;
  private final Set<String> usersSpeaking;

  public AudioReceiverHandler(DiscordUDPConnection udpConnection, MediaConnection connection) {
    this.connection = connection;
    this.udpConnection = udpConnection;
    this.usersSpeaking = new HashSet<>();
    this.silenceCount = new HashMap<>();
  }
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
    if (udpConnection.getSecretKey() == null) return;

    var buf = msg.content();
    if (buf.getByte(1) != OpusCodec.PAYLOAD_TYPE) return;

    long ssrc = buf.getUnsignedInt(8);
    String userId = connection.getGatewayConnection().getSsrcMap().get(ssrc);
    var usersToRecord = connection.getReceiveHandler().users();

    if (usersToRecord != null && !usersToRecord.contains(userId))
      return;

    AudioPacket audio = udpConnection.getEncryptionMode().open(
            buf,
            udpConnection.getSecretKey(),
            connection.getReceiveHandler().useDirectBuffer()
    );

    if (audio == null) {
      logger.debug("Failed to decrypt received audio frame");
      return;
    }

    var opus = audio.getOpusAudio();

    if (opus.get(0) == (byte)0xF8 && opus.get(1) == (byte)0xFF && opus.get(2) == (byte)0xFE) {
      if (!usersSpeaking.contains(userId)) return;
      short userSilenceCount = silenceCount.getOrDefault(userId, (short) 0);

      if (++userSilenceCount == 5) {
        usersSpeaking.remove(userId);
        silenceCount.remove(userId);
        connection.getDispatcher().userSpeakingStop(userId);
      }
      return;
    }

    usersSpeaking.add(userId);
    connection.getReceiveHandler().handleAudio(audio);
  }
}
