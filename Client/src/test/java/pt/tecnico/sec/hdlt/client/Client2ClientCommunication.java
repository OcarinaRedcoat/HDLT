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
        Client client3 = new Client(readUser(gridFileLocation, 3));
        UserServer userServer3 = new UserServer(client3);
        Client client11 = new Client(readUser(gridFileLocation, 11));
        UserServer userServer11 = new UserServer(client11);

        LocationReport locationReport = userClient1.requestLocationProofs(client1, 2L, 1);
        assertNotNull(locationReport);

        userServer3.stop();
        userServer11.stop();
    }

    @Test
    public void requestProofsWithoutEnoughWitnesses()
    {
        Client client1 = new Client(readUser(gridFileLocation, 1));
        UserClient userClient1 = new UserClient();
        Client client7 = new Client(readUser(gridFileLocation, 7));
        UserServer userServer7 = new UserServer(client7);

        LocationReport locationReport = userClient1.requestLocationProofs(client1, 4L, 1);
        assertNull(locationReport);

        userServer7.stop();
    }

    @Test
    public void requestProofsInvalidWitnessSignature()
    {
        Client client1 = new Client(readUser(gridFileLocation, 1));
        UserClient userClient1 = new UserClient();
        //private and public key of user 13 do not match
        Client client13 = new Client(readUser(gridFileLocation, 13));
        UserServer userServer13 = new UserServer(client13);

        LocationReport locationReport = userClient1.requestLocationProofs(client1, 16L, 0);
        assertNull(locationReport);

        userServer13.stop();
    }

    @Test
    public void requestProofsInvalidRequesterSignature()
    {
        //private and public key of user 13 do not match
        Client client13 = new Client(readUser(gridFileLocation, 13));
        UserClient userClient13 = new UserClient();
        Client client1 = new Client(readUser(gridFileLocation, 1));
        UserServer userServer1 = new UserServer(client1);

        LocationReport locationReport = userClient13.requestLocationProofs(client13, 16L, 0);
        assertNull(locationReport);

        userServer1.stop();
    }

}
