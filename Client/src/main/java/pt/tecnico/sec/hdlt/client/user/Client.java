package pt.tecnico.sec.hdlt.client.user;

import pt.tecnico.sec.hdlt.User;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import static pt.tecnico.sec.hdlt.FileUtils.*;
import static pt.tecnico.sec.hdlt.IOUtils.readPassword;
import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.getKeyPairFromKeyStore;

public class Client {

    private User user;
    private KeyPair keyPair;

    public Client(User user) {
        this.user = user;
        try{
            this.keyPair = getKeyPairFromKeyStore(
                    new File("../keys/client_" + user.getId() + ".jks"),
                    readPassword("Password: (default= client_N, N= client id)"),
                    "client_" + user.getId());
        } catch (NoSuchAlgorithmException | IOException | CertificateException |
                KeyStoreException | UnrecoverableKeyException e) {
            System.err.println("There was a problem reading the user RSA key pairs. Make sure the keyStore exists and is correct.");
            System.exit(1);
        }
    }

    public Client(User user, String password) {
        this.user = user;
        try{
            this.keyPair = getKeyPairFromKeyStore(
                    new File("../keys/client_" + user.getId() + ".jks"), password, "client_" + user.getId());
        } catch (NoSuchAlgorithmException | IOException | CertificateException |
                KeyStoreException | UnrecoverableKeyException e) {
            System.err.println("There was a problem reading the user RSA key pairs. Make sure the keyStore exists and is correct.");
            System.exit(1);
        }
    }

    public User getUser() {
        return user;
    }

    public PrivateKey getPrivKey() {
        return keyPair.getPrivate();
    }

    public PublicKey getPubKey() {
        return keyPair.getPublic();
    }

}
