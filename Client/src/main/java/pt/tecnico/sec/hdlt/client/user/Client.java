package pt.tecnico.sec.hdlt.client.user;

import pt.tecnico.sec.hdlt.FileUtils;
import pt.tecnico.sec.hdlt.User;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;


public class Client {

    private static Client INSTANCE = null;

    private User user;
    private PrivateKey privKey;
    private PublicKey pubKey;

    public Client() {
        this.user = null;
    }

    public static Client getInstance(){
        if (INSTANCE == null)
            INSTANCE = new Client();

        return INSTANCE;
    }

    public User getUser() {
        return user;
    }

    public void initializeUser(User user) throws  NoSuchAlgorithmException{
        try {
            this.user = user;
            this.pubKey = FileUtils.getUserPublicKey(this.user.getId());
            this.privKey = FileUtils.getUserPrivateKey(this.user.getId());
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e){
            System.out.println(e.getMessage());
        }
    }

    public PrivateKey getPrivKey() {
        return privKey;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

}
