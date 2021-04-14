package pt.tecnico.sec.hdlt.client;

import static org.junit.Assert.*;
import static pt.tecnico.sec.hdlt.IOUtils.readUser;

import org.json.simple.parser.ParseException;
import org.junit.Test;
import pt.tecnico.sec.hdlt.FileUtils;
import pt.tecnico.sec.hdlt.Position;
import pt.tecnico.sec.hdlt.User;
import pt.tecnico.sec.hdlt.client.communication.UserClient;
import pt.tecnico.sec.hdlt.client.communication.UserServer;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.LocationReport;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Unit test for simple App.
 */
public class AppTest 
{
    private static final String gridFileLocation = "../grids.output.json";

    @Test
    public void requestProofsSomeClientsDontRespond()
    {
        Client client1 = new Client(readUser(gridFileLocation, 1));
        UserClient userClient1 = new UserClient();
        Client client5 = new Client(readUser(gridFileLocation, 5));
        UserServer userServer5 = new UserServer(client5);
        Client client19 = new Client(readUser(gridFileLocation, 19));
        UserServer userServer19 = new UserServer(client19);

        LocationReport locationReport = userClient1.requestLocationProofs(client1, 1L, 1);
        assertNotNull(locationReport);
    }


}
