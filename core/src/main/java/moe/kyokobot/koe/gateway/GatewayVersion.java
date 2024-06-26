package moe.kyokobot.koe.gateway;

import moe.kyokobot.koe.VoiceServerInfo;
import moe.kyokobot.koe.internal.MediaConnectionImpl;

public enum GatewayVersion {
    V4(MediaGatewayV4Connection::new);
    private final MediaGatewayConnectionFactory factory;

    public MediaGatewayConnection createConnection(MediaConnectionImpl connection, VoiceServerInfo voiceServerInfo) {
        return factory.create(connection, voiceServerInfo);
    }

    GatewayVersion(MediaGatewayConnectionFactory factory) {
        this.factory = factory;
    }
}
