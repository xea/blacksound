package so.blacklight.blacksound.crypto;

import com.google.crypto.tink.Aead;
import com.google.crypto.tink.CleartextKeysetHandle;
import com.google.crypto.tink.JsonKeysetReader;
import com.google.crypto.tink.aead.AeadConfig;
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
                .mapTry(keysetHandle -> keysetHandle.getPrimitive(Aead.class))
                .map(AeadCrypto::new)
                .get();

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
