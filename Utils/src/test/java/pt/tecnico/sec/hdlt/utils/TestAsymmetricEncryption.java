package pt.tecnico.sec.hdlt.utils;

import org.junit.Test;
import pt.tecnico.sec.hdlt.FileUtils;
import pt.tecnico.sec.hdlt.crypto.CryptographicOperations;

import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TestAsymmetricEncryption {

    @Test
    public void validPrivateEncryptPublicDecrypt() {
        //TODO
//        String stringToEncrypt = "Hello World!";
//
//        try {
//            PrivateKey privateKey = FileUtils.getServerPrivateKey(1);
//            byte[] encryptedBytes = CryptographicOperations.asymmetricEncrypt(stringToEncrypt.getBytes(), privateKey);
//            PublicKey publicKey = FileUtils.getServerPublicKey(1);
//            byte[] decryptedBytes = CryptographicOperations.asymmetricDecrypt(encryptedBytes, publicKey);
//            String decryptedString = new String(decryptedBytes);
//
//            assertEquals(stringToEncrypt, decryptedString);
//        } catch (Exception e) {
//            fail();
//        }
    }

    @Test
    public void validPublicEncryptPrivateDecrypt() {
        //TODO
//        String stringToEncrypt = "Hello World!";
//
//        try {
//            PublicKey publicKey = FileUtils.getServerPublicKey(1);
//            byte[] encryptedBytes = CryptographicOperations.asymmetricEncrypt(stringToEncrypt.getBytes(), publicKey);
//            PrivateKey privateKey = FileUtils.getServerPrivateKey(1);
//            byte[] decryptedBytes = CryptographicOperations.asymmetricDecrypt(encryptedBytes, privateKey);
//            String decryptedString = new String(decryptedBytes);
//
//            assertEquals(stringToEncrypt, decryptedString);
//        } catch (Exception e) {
//            fail();
//        }
    }
}
