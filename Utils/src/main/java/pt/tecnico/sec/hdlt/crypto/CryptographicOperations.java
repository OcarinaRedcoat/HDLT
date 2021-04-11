package pt.tecnico.sec.hdlt.crypto;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class CryptographicOperations {

    // TODO Use IV in symmetric encrypt
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final int SYMMETRIC_KEY_SIZE = 256;
    private static final String ASYMMETRIC_ALGORITHM = "RSA";
    private static final int ASYMMETRIC_KEY_SIZE = 2048;
    private static final String SIGN_ALGORITHM = "SHA256withRSA";
    private static final String HASH_ALGORITHM = "SHA-256";

    public static SecretKey generateSecretKey(){
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        SecureRandom secureRandom = new SecureRandom();
        keyGenerator.init(SYMMETRIC_KEY_SIZE, secureRandom);
        return keyGenerator.generateKey();
    }

    public static SecretKey convertToSymmetricKey(byte[] key) {
        return new SecretKeySpec(key, SYMMETRIC_ALGORITHM);
    }

    public static PublicKey convertToPublicKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance(ASYMMETRIC_ALGORITHM).generatePublic(new X509EncodedKeySpec(key));
    }

    private static byte[] transform(int mode, String transformation, byte[] data, Key key) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(mode, key);
        return cipher.doFinal(data);
    }

    public static byte[] symmetricEncrypt(byte[] data, Key key)
            throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException,
            NoSuchPaddingException {

        return transform(Cipher.ENCRYPT_MODE, SYMMETRIC_ALGORITHM, data, key);
    }

    public static byte[] symmetricDecrypt(byte[] data, Key key)
            throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException,
            NoSuchPaddingException {

        return transform(Cipher.DECRYPT_MODE, SYMMETRIC_ALGORITHM, data, key);
    }

    public static byte[] asymmetricEncrypt(byte[] data, Key key)
            throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException,
            NoSuchPaddingException {

        return transform(Cipher.ENCRYPT_MODE, ASYMMETRIC_ALGORITHM, data, key);
    }

    public static byte[] asymmetricDecrypt(byte[] data, Key key)
            throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException,
            NoSuchPaddingException {

        return transform(Cipher.DECRYPT_MODE, ASYMMETRIC_ALGORITHM, data, key);
    }

    public static byte[] sign(byte[] data, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException,
            SignatureException {

        Signature signature = Signature.getInstance(SIGN_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(data);

        return signature.sign();
    }

    public static boolean verifySignature(PublicKey publicKey, byte[] message, byte[] signature)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {

        Signature sig = Signature.getInstance(SIGN_ALGORITHM);
        sig.initVerify(publicKey);
        sig.update(message);

        return sig.verify(signature);
    }

    public static String createMessageDigest(String data) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        return Base64.getEncoder().encodeToString(md.digest(data.getBytes()));
    }

    public static boolean verifyMessageDigest(String data, String digest) throws NoSuchAlgorithmException {
        String computedDigest = createMessageDigest(data);

        return computedDigest.equals(digest);
    }

    public static byte[] Hash(String digestAlg, byte[] data) throws Exception {
        System.out.println("Digesting with " + digestAlg + "...");
        MessageDigest messageDigest = MessageDigest.getInstance(digestAlg);
        messageDigest.update(data);
        byte[] digestBytes = messageDigest.digest();
        System.out.println("Result digest size: " + digestBytes.length + " bytes");
        String digestB64dString = Base64.getEncoder().encodeToString(digestBytes);
        System.out.println("Digest result, encoded as base 64 string: " + digestB64dString);
        return digestBytes;
    }
}
