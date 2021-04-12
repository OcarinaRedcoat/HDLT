package pt.tecnico.sec.hdlt.client.user;

import pt.tecnico.sec.hdlt.User;

import java.security.PrivateKey;
import java.security.PublicKey;

import static pt.tecnico.sec.hdlt.FileUtils.getUserPrivateKey;
import static pt.tecnico.sec.hdlt.FileUtils.getUserPublicKey;

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
        this.privKey = getUserPrivateKey(this.user.getId());
        this.pubKey = getUserPublicKey(this.user.getId());
    }

    public PrivateKey getPrivKey() {
        return privKey;
    }

    public PublicKey getPubKey() {
        return pubKey;
    }

}
