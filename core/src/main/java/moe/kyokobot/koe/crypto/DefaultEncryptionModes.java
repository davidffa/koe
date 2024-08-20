package moe.kyokobot.koe.crypto;

import java.security.Security;
import java.util.Map;
import java.util.function.Supplier;

class DefaultEncryptionModes {
    private DefaultEncryptionModes() {
        //
    }

    static final Map<String, Supplier<EncryptionMode>> encryptionModes;

    static {
        boolean aesSupported = Security.getAlgorithms("Cipher").contains("AES_256/GCM/NOPADDING");

        if (aesSupported) {
            encryptionModes = Map.of( // sorted by priority
                    "aead_aes256_gcm_rtpsize", AES256GCMEncryptionMode::new,
                    "aead_xchacha20_poly1305_rtpsize", XChaCha20Poly1305EncryptionMode::new,
                    "plain", PlainEncryptionMode::new // not supported by Discord anymore, implemented for testing.
            );
        } else {
            encryptionModes = Map.of( // sorted by priority
                    "aead_xchacha20_poly1305_rtpsize", XChaCha20Poly1305EncryptionMode::new,
                    "plain", PlainEncryptionMode::new // not supported by Discord anymore, implemented for testing.
            );
        }
    }
}
