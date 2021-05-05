package pt.tecnico.sec.hdlt.client;

import static org.junit.Assert.*;
import static pt.tecnico.sec.hdlt.utils.IOUtils.readUser;

import org.junit.Test;
import pt.tecnico.sec.hdlt.client.communication.UserClient;
import pt.tecnico.sec.hdlt.client.communication.UserServer;
import pt.tecnico.sec.hdlt.communication.LocationReport;
import pt.tecnico.sec.hdlt.entities.Client;


public class TestClient2ClientCommunication
{
    private static final String gridFileLocation = "../grids.output.json";

    @Test
    public void requestProofsWithEnoughWitnesses()
    {
        Client client1 = new Client(readUser(gridFileLocation, 1), "client_1");
        UserClient userClient1 = new UserClient(client1);
        Client client3 = new Client(readUser(gridFileLocation, 3), "client_3");
        UserServer userServer3 = new UserServer(client3);
        Client client11 = new Client(readUser(gridFileLocation, 11), "client_11");
        UserServer userServer11 = new UserServer(client11);

        LocationReport locationReport = userClient1.requestLocationProofs(2L, 1).build();
        assertNotNull(locationReport);

        userServer3.stop();
        userServer11.stop();
    }

    @Test
    public void requestProofsWithoutEnoughWitnesses()
    {
        Client client1 = new Client(readUser(gridFileLocation, 1), "client_1");
        UserClient userClient1 = new UserClient(client1);
        Client client7 = new Client(readUser(gridFileLocation, 7), "client_7");
        UserServer userServer7 = new UserServer(client7);

        LocationReport locationReport = userClient1.requestLocationProofs(4L, 1).build();
        assertNull(locationReport);

        userServer7.stop();
    }

    @Test
    public void requestProofsInvalidWitnessSignature()
    {
        Client client1 = new Client(readUser(gridFileLocation, 1), "client_1");
        UserClient userClient1 = new UserClient(client1);
        //private and public key of user 13 do not match
        Client client13 = new Client(readUser(gridFileLocation, 13), "client_13");
        UserServer userServer13 = new UserServer(client13);

        LocationReport locationReport = userClient1.requestLocationProofs(16L, 0).build();
        assertNull(locationReport);

        userServer13.stop();
    }

    @Test
    public void requestProofsInvalidRequesterSignature()
    {
        //private and public key of user 13 do not match
        Client client13 = new Client(readUser(gridFileLocation, 13), "client_13");
        UserClient userClient13 = new UserClient(client13);
        Client client1 = new Client(readUser(gridFileLocation, 1), "client_1");
        UserServer userServer1 = new UserServer(client1);

        LocationReport locationReport = userClient13.requestLocationProofs(16L, 0).build();
        assertNull(locationReport);

        userServer1.stop();
    }

}
