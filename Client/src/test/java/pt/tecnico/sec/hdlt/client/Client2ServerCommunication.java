package pt.tecnico.sec.hdlt.client;

import org.junit.Test;
import pt.tecnico.sec.hdlt.client.communication.UserClient;
import pt.tecnico.sec.hdlt.client.communication.UserServer;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.LocationReport;

import static org.junit.Assert.*;
import static pt.tecnico.sec.hdlt.IOUtils.readUser;

public class Client2ServerCommunication {

    private static final String gridFileLocation = "../grids.output.json";

    @Test
    public void submitReport()
    {
        Client client1 = new Client(readUser(gridFileLocation, 1));
        UserClient userClient1 = new UserClient();
        Client client3 = new Client(readUser(gridFileLocation, 3));
        UserServer userServer3 = new UserServer(client3);
        Client client9 = new Client(readUser(gridFileLocation, 9));
        UserServer userServer9 = new UserServer(client9);
        Client client12 = new Client(readUser(gridFileLocation, 12));
        UserServer userServer12 = new UserServer(client12);
        Client client15 = new Client(readUser(gridFileLocation, 15));
        UserServer userServer15 = new UserServer(client15);
        Client client17 = new Client(readUser(gridFileLocation, 17));
        UserServer userServer17 = new UserServer(client17);

        //TODO: start server
        //Server server = new Server(1);

        LocationReport locationReport = userClient1.requestLocationProofs(client1, 14L, 4);
        Boolean submitedReport = userClient1.submitLocationReport(client1, locationReport);
        assertEquals(true, submitedReport);

        userServer3.stop();
        userServer9.stop();
        userServer12.stop();
        userServer15.stop();
        userServer17.stop();
    }

    @Test
    public void getReport()
    {
        Client client1 = new Client(readUser(gridFileLocation, 1));
        UserClient userClient1 = new UserClient();

        //TODO: start server
        //Server server = new Server(1);

        LocationReport locationReport = userClient1.obtainLocationReport(client1, 14L);
        assertNotNull(locationReport);
    }

    //TODO:
    @Test
    public void getReportServerInvalidSignature()
    {

    }
}
