package pt.tecnico.sec.hdlt.utils;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.NoSuchElementException;

public class CryptographicUtils {

    private static final String SYMMETRIC_ALGORITHM_WITH_PADDING = "AES/CBC/PKCS5Padding";
    private static final String SYMMETRIC_ALGORITHM = "AES";
    private static final int SYMMETRIC_KEY_SIZE = 256;
    private static final int SYMMETRIC_BLOCK_SIZE = 128; //Block size for iv, for AES it is 128 / 8
    private static final String ASYMMETRIC_ALGORITHM = "RSA";
    private static final int ASYMMETRIC_KEY_SIZE = 2048;
    private static final String SIGN_ALGORITHM = "SHA256withRSA";
    private static final String HASH_ALGORITHM = "SHA-256";
    private static final int NONCE_SIZE = 30;
    private static final String LEADINGPOWZEROS = "000";

    public static SecretKey generateSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(SYMMETRIC_ALGORITHM);
        SecureRandom secureRandom = new SecureRandom();
        keyGenerator.init(SYMMETRIC_KEY_SIZE, secureRandom);
        return keyGenerator.generateKey();
    }

    public static IvParameterSpec generateIv() {
        SecureRandom randomSecureRandom = new SecureRandom();
        byte[] iv = new byte[SYMMETRIC_BLOCK_SIZE / 8];
        randomSecureRandom.nextBytes(iv);
        return new IvParameterSpec(iv);
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

    private static byte[] transform(int mode, String transformation, byte[] data, Key key, IvParameterSpec iv) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException {

        Cipher cipher = Cipher.getInstance(transformation);
        cipher.init(mode, key, iv);
        return cipher.doFinal(data);
    }

    public static byte[] symmetricEncrypt(byte[] data, Key key, IvParameterSpec iv)
            throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidAlgorithmParameterException {

        return transform(Cipher.ENCRYPT_MODE, SYMMETRIC_ALGORITHM_WITH_PADDING, data, key, iv);
    }

    public static byte[] symmetricDecrypt(byte[] data, Key key, IvParameterSpec iv)
            throws IllegalBlockSizeException, InvalidKeyException, BadPaddingException, NoSuchAlgorithmException,
            NoSuchPaddingException, InvalidAlgorithmParameterException {

        return transform(Cipher.DECRYPT_MODE, SYMMETRIC_ALGORITHM_WITH_PADDING, data, key, iv);
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

    public static String createMessageDigest(Object obj) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance(HASH_ALGORITHM);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return Base64.getEncoder().encodeToString(md.digest(out.toByteArray()));
    }

    public static boolean verifyMessageDigest(String data, String digest) throws NoSuchAlgorithmException {
        String computedDigest = createMessageDigest(data);
        return computedDigest.equals(digest);
    }

    public static KeyPair getKeyPairFromKeyStore(File keystoreFile, String password, String alias)
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, UnrecoverableKeyException {

        KeyStore keyStore = FileUtils.loadKeyStore(keystoreFile, password);
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
        Certificate cert = keyStore.getCertificate(alias);

        return new KeyPair(cert.getPublicKey(), privateKey);
    }

    public static PrivateKey getPrivateKey(File keystoreFile, String keyStorePassword, String keyAlias)
            throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException,
            UnrecoverableKeyException {

        Key key = FileUtils.loadKeyStore(keystoreFile, keyStorePassword).getKey(keyAlias, keyStorePassword.toCharArray());

        if (key instanceof PrivateKey) {
            return (PrivateKey) key;
        }

        throw new NoSuchElementException("Private key " + keyAlias + " not found");
    }

    public static PrivateKey getServerPrivateKey(int serverId, String keyStorePassword) throws Exception {
        String aux = "server_" + serverId;
        return getPrivateKey(new File("../keys/" + aux + ".jks"), keyStorePassword, aux);
    }

    public static String generateNonce(){
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < NONCE_SIZE; i++) {
            stringBuilder.append(secureRandom.nextInt(10));
        }
        return stringBuilder.toString();
    }

    public static Boolean isValidPoW(Object obj) throws IOException, NoSuchAlgorithmException {
        String hash = createMessageDigest(obj);
        return hash.startsWith(LEADINGPOWZEROS);
    }
}
