package pt.tecnico.sec.hdlt.client.user;

import pt.tecnico.sec.hdlt.User;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;

import static pt.tecnico.sec.hdlt.FileUtils.*;

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

    public void initializeUser(User user) {
        this.user = user;
        try{
            this.pubKey = getUserPublicKey(this.user.getId());
            this.privKey = getUserPrivateKey(this.user.getId());
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            System.err.println("There was a problem reading the user private and public RSA keys. Make sure they exist and are in the correct format.");
            System.exit(1);
        }
    }

    public PrivateKey getPrivKey() {
        return privKey;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

}
