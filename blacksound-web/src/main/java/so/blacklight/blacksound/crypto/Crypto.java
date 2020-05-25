package so.blacklight.blacksound.crypto;

import io.vavr.control.Validation;

public interface Crypto {

    String encryptAndEncode64(final byte[] data);

    Validation<String, byte[]> decode64AndDecrypt(final String data);
}
