package pt.tecnico.sec.hdlt.client.user;

import pt.tecnico.sec.hdlt.User;

public class Client {

    private static Client INSTANCE = null;

    private User user;

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

    public void setUser(User user) {
        this.user = user;
    }
}
