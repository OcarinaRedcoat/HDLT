package pt.tecnico.sec.hdlt.client;

import static org.junit.Assert.*;
import static pt.tecnico.sec.hdlt.utils.IOUtils.readUser;

import org.junit.Test;
import pt.tecnico.sec.hdlt.client.services.UserClient;
import pt.tecnico.sec.hdlt.client.services.Server;
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
        Server server3 = new Server(client3);
        Client client11 = new Client(readUser(gridFileLocation, 11), "client_11");
        Server server11 = new Server(client11);

        LocationReport locationReport = userClient1.requestLocationProofs(2L, 1).build();
        assertNotNull(locationReport);

        userClient1.closeServerChannel();
        server3.stop();
        server11.stop();
    }

    @Test
    public void requestProofsWithoutEnoughWitnesses()
    {
        Client client1 = new Client(readUser(gridFileLocation, 1), "client_1");
        UserClient userClient1 = new UserClient(client1);
        Client client7 = new Client(readUser(gridFileLocation, 7), "client_7");
        Server server7 = new Server(client7);

        LocationReport.Builder builder = userClient1.requestLocationProofs(4L, 1);
        assertNull(builder);

        userClient1.closeServerChannel();
        server7.stop();
    }

    @Test
    public void requestProofsInvalidWitnessSignature()
    {
        Client client1 = new Client(readUser(gridFileLocation, 1), "client_1");
        UserClient userClient1 = new UserClient(client1);
        //private and public key of user 13 do not match
        Client client13 = new Client(readUser(gridFileLocation, 13), "client_13");
        Server server13 = new Server(client13);

        LocationReport.Builder builder = userClient1.requestLocationProofs(16L, 0);
        assertNull(builder);

        userClient1.closeServerChannel();
        server13.stop();
    }

    @Test
    public void requestProofsInvalidRequesterSignature()
    {
        //private and public key of user 13 do not match
        Client client13 = new Client(readUser(gridFileLocation, 13), "client_13");
        UserClient userClient13 = new UserClient(client13);
        Client client1 = new Client(readUser(gridFileLocation, 1), "client_1");
        Server server1 = new Server(client1);

        LocationReport.Builder builder = userClient13.requestLocationProofs(16L, 0);
        assertNull(builder);

        userClient13.closeServerChannel();
        server1.stop();
    }

}
