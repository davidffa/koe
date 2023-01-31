package moe.kyokobot.koe.codec;

import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class Codec {
    protected final String name;
    protected final byte payloadType;
    protected final int priority;

    protected Codec(String name, byte payloadType, int priority) {
        this.name = name;
        this.payloadType = payloadType;
        this.priority = priority;
    }

    public String getName() {
        return name;
    }

    public byte getPayloadType() {
        return payloadType;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Codec that = (Codec) o;
        return payloadType == that.payloadType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(payloadType);
    }

    /**
     * Gets audio codec description by name.
     *
     * @param name the codec name
     * @return Codec instance or null if the codec is not found/supported by Koe.
     */
    @Nullable
    public static Codec getAudio(String name) {
        return DefaultCodecs.audioCodecs.get(name);
    }

    /**
     * Gets audio codec by payload type.
     *
     * @param payloadType the payload type
     * @return Codec instance or null if the codec is not found/supported by Koe.
     */
    @Nullable
    public static Codec getByPayload(byte payloadType) {
        for (var codec : DefaultCodecs.audioCodecs.values()) {
            if (codec.getPayloadType() == payloadType) {
                return codec;
            }
        }

        return null;
    }
}
