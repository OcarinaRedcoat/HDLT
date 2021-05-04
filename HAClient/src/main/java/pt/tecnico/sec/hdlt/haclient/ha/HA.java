package pt.tecnico.sec.hdlt.haclient.ha;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import static pt.tecnico.sec.hdlt.FileUtils.*;
import static pt.tecnico.sec.hdlt.IOUtils.readPassword;
import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.getKeyPairFromKeyStore;

public class HA {

    private static HA INSTANCE = null;

    private KeyPair keyPair;

    private HA() {
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


    public static HA getInstance(){
        if (INSTANCE == null){
            INSTANCE = new HA();
        }
        return INSTANCE;
    }

}