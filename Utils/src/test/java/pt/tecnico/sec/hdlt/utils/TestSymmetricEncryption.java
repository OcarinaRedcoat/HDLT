package pt.tecnico.sec.hdlt.utils;

import org.junit.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestSymmetricEncryption {

    @Test
    public void validEncryptDecrypt() {
        String stringToEncrypt = "Hello World!";

        try {
            SecretKey key = CryptographicUtils.generateSecretKey();
            IvParameterSpec iv = CryptographicUtils.generateIv();

            byte[] encryptedBytes = CryptographicUtils.symmetricEncrypt(stringToEncrypt.getBytes(), key, iv);
            byte[] decryptedBytes = CryptographicUtils.symmetricDecrypt(encryptedBytes, key, iv);
            String decryptedString = new String(decryptedBytes);

            assertEquals(stringToEncrypt, decryptedString);
        } catch (Exception e) {
            fail();
        }
    }
}