package pt.tecnico.sec.hdlt.client;

import pt.tecnico.sec.hdlt.client.user.User;
import pt.tecnico.sec.hdlt.client.user.UserClient;
import pt.tecnico.sec.hdlt.client.user.UserServer;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class Main
{
    public static void main( String[] args) throws Exception {
        String grid_filename = args[0];

        int user_id = Integer.parseInt(args[1]); // I am user user xxx
        Grid grid = new Grid(); // change hard coded
        grid.parseFiles(grid_filename);

        // -------------------- Epoch -------------------
        //Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        //long currentTimeStep = Math.floorDiv(calendar.getTimeInMillis() / 1000, TIME_STEP)

        User myUser = grid.getMyUser(user_id, 0);

        UserServer server = new UserServer(myUser);

        server.start();
        server.blockUntilShutdown(); //??????? No git tem não sei se aqui faz sentido

        ArrayList<User> users_that_are_close = grid.usersClosedBy(myUser.getCloseBy(),0);

        UserClient client = new UserClient(myUser, users_that_are_close);

        client.requestLocationProof(myUser, 0);

        // Fazer o shutdown dos channels ......
    }
}
