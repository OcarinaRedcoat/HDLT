package pt.tecnico.sec.hdlt.utils;

import org.junit.Test;
import pt.tecnico.sec.hdlt.crypto.CryptographicOperations;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestSymmetricEncryption {

    @Test
    public void validEncryptDecrypt() {
        String stringToEncrypt = "Hello World!";

        try {
            SecretKey key = CryptographicOperations.generateSecretKey();
            IvParameterSpec iv = CryptographicOperations.generateIv();

            byte[] encryptedBytes = CryptographicOperations.symmetricEncrypt(stringToEncrypt.getBytes(), key, iv);
            byte[] decryptedBytes = CryptographicOperations.symmetricDecrypt(encryptedBytes, key, iv);
            String decryptedString = new String(decryptedBytes);

            assertEquals(stringToEncrypt, decryptedString);
        } catch (Exception e) {
            fail();
        }
    }
}