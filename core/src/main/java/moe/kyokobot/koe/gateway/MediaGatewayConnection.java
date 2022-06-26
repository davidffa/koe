package moe.kyokobot.koe.gateway;

import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface MediaGatewayConnection {
    Map<Long, String> getSsrcMap();
    long getPing();

    boolean isOpen();

    CompletableFuture<Void> start();

    void close(int code, @Nullable String reason);

    void reconnect();

    void updateSpeaking(int mask);
}
