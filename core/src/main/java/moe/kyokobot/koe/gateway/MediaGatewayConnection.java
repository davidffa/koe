package moe.kyokobot.koe.gateway;

import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public interface MediaGatewayConnection {
    long getPing();

    boolean isOpen();

    CompletableFuture<Void> start();

    void close(int code, @Nullable String reason, boolean reconnect);

    CompletableFuture<Void> reconnect();

    void updateSpeaking(int mask);
}
