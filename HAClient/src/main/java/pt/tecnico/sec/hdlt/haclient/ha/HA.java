package pt.tecnico.sec.hdlt.haclient.ha;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

import static pt.tecnico.sec.hdlt.utils.IOUtils.readPassword;
import static pt.tecnico.sec.hdlt.utils.CryptographicUtils.getKeyPairFromKeyStore;

public class HA {

    private KeyPair keyPair;

    public HA() {
        try{
            this.keyPair = getKeyPairFromKeyStore(
                    new File("../keys/ha_1.jks"),
                    readPassword("Password: (default= ha_keystore_1)"),
                    "ha_1");
        } catch (IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException |
                KeyStoreException e) {
            System.err.println("There was a problem reading the ha private and public RSA keys. Make sure they exist and are in the correct format.");
            System.exit(1);
        }
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

}