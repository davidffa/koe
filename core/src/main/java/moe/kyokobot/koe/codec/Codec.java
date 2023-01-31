package moe.kyokobot.koe.codec;

import moe.kyokobot.koe.internal.json.JsonObject;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class Codec {
    protected final String name;
    protected final byte payloadType;
    protected final byte rtxPayloadType;
    protected final int priority;
    protected final JsonObject jsonDescription;

    protected Codec(String name, byte payloadType, int priority) {
        this(name, payloadType, (byte) 0, priority);
    }

    protected Codec(String name, byte payloadType, byte rtxPayloadType, int priority) {
        this.name = name;
        this.payloadType = payloadType;
        this.rtxPayloadType = rtxPayloadType;
        this.priority = priority;

        this.jsonDescription = new JsonObject()
                .add("name", name)
                .add("payload_type", payloadType)
                .add("priority", priority);
    }

    public String getName() {
        return name;
    }

    public byte getPayloadType() {
        return payloadType;
    }

    public byte getRetransmissionPayloadType() {
        return rtxPayloadType;
    }

    public int getPriority() {
        return priority;
    }

    public JsonObject getJsonDescription() {
        return jsonDescription;
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
