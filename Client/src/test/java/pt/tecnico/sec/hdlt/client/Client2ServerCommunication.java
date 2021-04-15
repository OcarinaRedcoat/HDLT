package pt.tecnico.sec.hdlt.client;

import org.junit.Test;
import pt.tecnico.sec.hdlt.client.communication.UserClient;
import pt.tecnico.sec.hdlt.client.communication.UserServer;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.LocationReport;
import pt.tecnico.sec.hdlt.server.LocationServer;
import pt.tecnico.sec.hdlt.server.bll.LocationBL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import static org.junit.Assert.*;
import static pt.tecnico.sec.hdlt.IOUtils.readUser;

public class Client2ServerCommunication {

    private static final String gridFileLocation = "../grids.output.json";
    private static final String serverFileLocation = "../server_1.txt";

    private LocationServer server;

    private void startServer(){
        this.server = new LocationServer();
        try {
            LocationBL locationBL = new LocationBL(1, 2);
            this.server.start(locationBL);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    private void stopServer(){
        try {
            server.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void submitReportAndGetReport()
    {
        Client client1 = new Client(readUser(gridFileLocation, 1));
        UserClient userClient1 = new UserClient();
        Client client3 = new Client(readUser(gridFileLocation, 3));
        UserServer userServer3 = new UserServer(client3);
        Client client9 = new Client(readUser(gridFileLocation, 9));
        UserServer userServer9 = new UserServer(client9);
        Client client17 = new Client(readUser(gridFileLocation, 17));
        UserServer userServer17 = new UserServer(client17);

        startServer();

        LocationReport locationReport = userClient1.requestLocationProofs(client1, 14L, 2);
        Boolean submitedReport = userClient1.submitLocationReport(client1, locationReport);
        assertEquals(true, submitedReport);

        locationReport = userClient1.obtainLocationReport(client1, 14L);
        assertNotNull(locationReport);

        userServer3.stop();
        userServer9.stop();
        userServer17.stop();
        stopServer();
    }
}
