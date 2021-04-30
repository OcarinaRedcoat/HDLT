package pt.tecnico.sec.hdlt.utils;

import org.junit.Test;
import pt.tecnico.sec.hdlt.FileUtils;
import pt.tecnico.sec.hdlt.crypto.CryptographicOperations;

import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestSignature {

    @Test
    public void validSignature() {
        //TODO
//        String stringToEncrypt = "Hello World!";
//
//        try {
//            PrivateKey privateKey = FileUtils.getServerPrivateKey(1);
//            byte[] signature = CryptographicOperations.sign(stringToEncrypt.getBytes(), privateKey);
//            PublicKey publicKey = FileUtils.getServerPublicKey(1);
//            boolean valid = CryptographicOperations.verifySignature(publicKey, stringToEncrypt.getBytes(), signature);
//
//            assertTrue(valid);
//        } catch (Exception e) {
//            fail();
//        }
    }
}
