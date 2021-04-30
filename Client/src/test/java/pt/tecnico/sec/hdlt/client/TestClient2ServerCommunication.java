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

import static org.junit.Assert.*;
import static pt.tecnico.sec.hdlt.IOUtils.readUser;

public class TestClient2ServerCommunication {

    private static final String gridFileLocation = "../grids.output.json";

    private LocationServer server;
    private LocationBL locationBL;

    private void startServer(){
        this.server = new LocationServer();
        try {
            this.locationBL = new LocationBL(1, "server_1");
            this.server.start(locationBL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopServer(){
        try {
            this.server.stop();
            this.locationBL.terminateMessageWriteQueue();
            this.locationBL.terminateNonceWriteQueue();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void deleteServerReports(){
        Path fileToDeletePath = Paths.get("../Server/src/main/resources/server_1.txt");
        try {
            Files.deleteIfExists(fileToDeletePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void submitReportAndGetReport()
    {
        deleteServerReports();

        Client client1 = new Client(readUser(gridFileLocation, 1), "client_1");
        UserClient userClient1 = new UserClient();
        Client client6 = new Client(readUser(gridFileLocation, 6), "client_6");
        UserServer userServer6 = new UserServer(client6);
        Client client12 = new Client(readUser(gridFileLocation, 12), "client_12");
        UserServer userServer12 = new UserServer(client12);
        Client client19 = new Client(readUser(gridFileLocation, 19), "client_19");
        UserServer userServer19 = new UserServer(client19);

        startServer();

        LocationReport locationReport = userClient1.requestLocationProofs(client1, 24L, 2);
        Boolean submitedReport = userClient1.submitLocationReport(client1, locationReport);
        assertEquals(true, submitedReport);

        locationReport = userClient1.obtainLocationReport(client1, 24L);
        assertNotNull(locationReport);

        userServer6.stop();
        userServer12.stop();
        userServer19.stop();
        stopServer();
    }
}
