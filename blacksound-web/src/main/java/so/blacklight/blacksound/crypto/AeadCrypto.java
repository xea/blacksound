package so.blacklight.blacksound.crypto;

import com.google.crypto.tink.*;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.aead.AeadKeyTemplates;
import io.vavr.control.Try;
import io.vavr.control.Validation;

import java.util.Base64;

public class AeadCrypto implements Crypto {

    private final Aead aead;

    private AeadCrypto(final Aead aead) {
        // Direct initialisation is not allowed
        this.aead = aead;
    }

    public static AeadCrypto getInstance() {
        Try.run(AeadConfig::register);

        // Attempt to load encryption keys
        return Try.of(() -> CleartextKeysetHandle.read(JsonKeysetReader.withPath("keyset.json")))
                .orElse(AeadCrypto::generateKeys)
                .mapTry(keysetHandle -> keysetHandle.getPrimitive(Aead.class))
                .map(AeadCrypto::new)
                .get();

    }

    private static Try<KeysetHandle> generateKeys() {
        return Try.of(() -> KeysetHandle.generateNew(AeadKeyTemplates.AES128_GCM))
                .mapTry(keysetHandle -> {
                    CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withPath("keyset.json"));
                    return keysetHandle;
                });
    }

    @Override
    public String encryptAndEncode64(byte[] data) {
        return Try.of(() -> aead.encrypt(data, new byte[] {}))
                .map(Base64.getEncoder()::encode)
                .map(String::new)
                .get();
    }

    @Override
    public Validation<String, byte[]> decode64AndDecrypt(final String data) {
        final var decoded64 = Base64.getDecoder().decode(data);

        return Try.of(() -> aead.decrypt(decoded64, new byte[] {}))
                .toValidation(Throwable::getMessage);
    }

}
