package moe.kyokobot.koe.codec;

import java.util.Map;

// todo: migrate to codec registry or something
public class DefaultCodecs {
    private DefaultCodecs() {
        //
    }

    public static final Map<String, Codec> audioCodecs;
    
    static {
        audioCodecs = Map.of(
                "opus", OpusCodec.INSTANCE
        );
    }
}
