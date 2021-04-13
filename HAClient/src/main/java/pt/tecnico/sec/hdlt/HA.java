-package pt.tecnico.sec.hdlt;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import static pt.tecnico.sec.hdlt.FileUtils.*;

public class HA {

    private static HA INSTANCE = null;

    private int ha_id;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    private HA(int id){ this.ha_id = id; }

    public static HA getInstance(){
        if (INSTANCE == null){
            INSTANCE = new HA(1);
        }
        return INSTANCE;
    }

    public void initializeHA() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        this.privateKey = getHAPrivateKey(this.ha_id);
        this.publicKey = getUserPublicKey(this.ha_id);
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() { return publicKey; }

    public int getHAId(){ return ha_id; }

}