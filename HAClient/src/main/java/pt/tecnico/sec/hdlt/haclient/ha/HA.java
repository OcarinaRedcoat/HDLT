package pt.tecnico.sec.hdlt.haclient.ha;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import static pt.tecnico.sec.hdlt.FileUtils.*;

public class HA {

    private static HA INSTANCE = null;

    private PrivateKey privateKey;
    private PublicKey publicKey;

    private HA() {
        try{
            this.privateKey = getHAPrivateKey();
            this.publicKey = getHAPublicKey();
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            System.err.println("There was a problem reading the ha private and public RSA keys. Make sure they exist and are in the correct format.");
            System.exit(1);
        }
    }


    public static HA getInstance(){
        if (INSTANCE == null){
            INSTANCE = new HA();
        }
        return INSTANCE;
    }

    public void initializeHA() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        this.privateKey = getHAPrivateKey();
        this.publicKey = getHAPublicKey();
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() { return publicKey; }


}