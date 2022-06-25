package moe.kyokobot.koe.internal.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import moe.kyokobot.koe.MediaConnection;
import moe.kyokobot.koe.codec.OpusCodec;
import moe.kyokobot.koe.internal.util.AudioPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AudioReceiverHandler extends SimpleChannelInboundHandler<DatagramPacket> {
  private static final Logger logger = LoggerFactory.getLogger(AudioReceiverHandler.class);
  private final DiscordUDPConnection udpConnection;
  private final MediaConnection connection;

  public AudioReceiverHandler(DiscordUDPConnection udpConnection, MediaConnection connection) {
    this.connection = connection;
    this.udpConnection = udpConnection;
  }
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
    if (this.udpConnection.getEncryptionMode() == null) return;

    var buf = msg.content();

    if (buf.getByte(1) != OpusCodec.PAYLOAD_TYPE) return;

    AudioPacket audio = this.udpConnection.getEncryptionMode().open(
            buf,
            this.udpConnection.getSecretKey(),
            this.connection.getReceiveHandler().useDirectBuffer()
    );

    if (audio == null) {
      logger.debug("Failed to decrypt received audio frame");
      return;
    }

    this.connection.getReceiveHandler().handleAudio(audio);
  }
}
