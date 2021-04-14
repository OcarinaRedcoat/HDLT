package pt.tecnico.sec.hdlt.client;

import static org.junit.Assert.*;
import static pt.tecnico.sec.hdlt.IOUtils.readUser;

import org.junit.Test;
import pt.tecnico.sec.hdlt.client.communication.UserClient;
import pt.tecnico.sec.hdlt.client.communication.UserServer;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.LocationReport;


public class Client2ClientCommunication
{
    private static final String gridFileLocation = "../grids.output.json";

    @Test
    public void requestProofsWithEnoughWitnesses()
    {
        Client client1 = new Client(readUser(gridFileLocation, 1));
        UserClient userClient1 = new UserClient();
        Client client5 = new Client(readUser(gridFileLocation, 5));
        UserServer userServer5 = new UserServer(client5);
        Client client19 = new Client(readUser(gridFileLocation, 19));
        UserServer userServer19 = new UserServer(client19);

        LocationReport locationReport = userClient1.requestLocationProofs(client1, 1L, 1);
        assertNotNull(locationReport);

        userServer5.stop();
        userServer19.stop();
    }

    @Test
    public void requestProofsWithoutEnoughWitnesses()
    {
        Client client1 = new Client(readUser(gridFileLocation, 1));
        UserClient userClient1 = new UserClient();
        Client client9 = new Client(readUser(gridFileLocation, 9));
        UserServer userServer9 = new UserServer(client9);

        LocationReport locationReport = userClient1.requestLocationProofs(client1, 2L, 1);
        assertNull(locationReport);

        userServer9.stop();
    }

    @Test
    public void requestProofsInvalidWitnessSignature()
    {
        Client client1 = new Client(readUser(gridFileLocation, 1));
        UserClient userClient1 = new UserClient();
        //private and public key of user 6 do not match
        Client client6 = new Client(readUser(gridFileLocation, 6));
        UserServer userServer6 = new UserServer(client6);

        LocationReport locationReport = userClient1.requestLocationProofs(client1, 7L, 0);
        assertNull(locationReport);

        userServer6.stop();
    }

    @Test
    public void requestProofsInvalidRequesterSignature()
    {
        //private and public key of user 6 do not match
        Client client6 = new Client(readUser(gridFileLocation, 6));
        UserClient userClient6 = new UserClient();
        Client client1 = new Client(readUser(gridFileLocation, 1));
        UserServer userServer1 = new UserServer(client1);

        LocationReport locationReport = userClient6.requestLocationProofs(client6, 8L, 0);
        assertNull(locationReport);

        userServer1.stop();
    }

}
