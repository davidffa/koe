package moe.kyokobot.koe.internal.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;
import moe.kyokobot.koe.MediaConnection;
import moe.kyokobot.koe.codec.OpusCodec;
import moe.kyokobot.koe.internal.util.AudioPacket;

public class AudioReceiver extends SimpleChannelInboundHandler<DatagramPacket> {
  private final DiscordUDPConnection udpConnection;
  private final MediaConnection connection;

  public AudioReceiver(DiscordUDPConnection udpConnection, MediaConnection connection) {
    this.connection = connection;
    this.udpConnection = udpConnection;
  }
  @Override
  protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket msg) {
    var buf = msg.content();

    if (buf.getByte(1) != OpusCodec.PAYLOAD_TYPE) return;

    AudioPacket audio = this.udpConnection.getEncryptionMode().open(buf, this.udpConnection.getSecretKey());

    if (audio == null) {
      System.out.println("Failed to decrypt the packet!");
      return;
    }

    this.connection.getReceiveHandler().handleAudio(audio);
  }
}
