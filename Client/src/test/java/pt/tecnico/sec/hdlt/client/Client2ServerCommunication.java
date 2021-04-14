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
        Client client5 = new Client(readUser(gridFileLocation, 5));
        UserServer userServer5 = new UserServer(client5);
        Client client8 = new Client(readUser(gridFileLocation, 8));
        UserServer userServer8 = new UserServer(client8);
        Client client11 = new Client(readUser(gridFileLocation, 11));
        UserServer userServer11 = new UserServer(client11);
        Client client12 = new Client(readUser(gridFileLocation, 12));
        UserServer userServer12 = new UserServer(client12);
        Client client13 = new Client(readUser(gridFileLocation, 13));
        UserServer userServer13 = new UserServer(client13);

        //TODO: start server
        //Server server = new Server(1);

        LocationReport locationReport = userClient1.requestLocationProofs(client1, 1L, 4);
        Boolean submitedReport = userClient1.submitLocationReport(client1, locationReport);
        assertEquals(true, submitedReport);

        userServer5.stop();
        userServer8.stop();
        userServer11.stop();
        userServer12.stop();
        userServer13.stop();
    }

    @Test
    public void getReport()
    {
        Client client1 = new Client(readUser(gridFileLocation, 1));
        UserClient userClient1 = new UserClient();

        //TODO: start server
        //Server server = new Server(1);

        LocationReport locationReport = userClient1.obtainLocationReport(client1, 1L);
        assertNotNull(locationReport);
    }

    //TODO:
    @Test
    public void getReportServerInvalidSignature()
    {

    }
}
