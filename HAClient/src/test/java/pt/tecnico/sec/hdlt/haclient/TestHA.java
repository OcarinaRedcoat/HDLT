package pt.tecnico.sec.hdlt.haclient;

import org.junit.Test;
import pt.tecnico.sec.hdlt.client.communication.UserClient;
import pt.tecnico.sec.hdlt.client.communication.UserServer;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.LocationReport;
import pt.tecnico.sec.hdlt.communication.SignedLocationReport;
import pt.tecnico.sec.hdlt.haclient.communication.HAClient;
import pt.tecnico.sec.hdlt.server.LocationServer;
import pt.tecnico.sec.hdlt.server.bll.LocationBL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;

import static org.junit.Assert.*;
import static pt.tecnico.sec.hdlt.IOUtils.readUser;

public class TestHA
{

    private static final String gridFileLocation = "../grids.output.json";

    private LocationServer server;
    private LocationBL locationBL;

    private void startServer(int numberByzantineUsers){
        this.server = new LocationServer();
        try {
            this.locationBL = new LocationBL(1, numberByzantineUsers);
            this.server.start(locationBL);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

    private void stopServer(){
        try {
            server.stop();
            this.locationBL.terminateWriteQueue();
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
    public void obtainLocationReport()
    {
        deleteServerReports();

        Client client1 = new Client(readUser(gridFileLocation, 1));
        UserClient userClient1 = new UserClient();
        Client client3 = new Client(readUser(gridFileLocation, 3));
        UserServer userServer3 = new UserServer(client3);
        Client client9 = new Client(readUser(gridFileLocation, 9));
        UserServer userServer9 = new UserServer(client9);
        Client client17 = new Client(readUser(gridFileLocation, 17));
        UserServer userServer17 = new UserServer(client17);

        startServer(2);

        LocationReport locationReport = userClient1.requestLocationProofs(client1, 14L, 2);
        userClient1.submitLocationReport(client1, locationReport);

        userServer3.stop();
        userServer9.stop();
        userServer17.stop();


        HAClient haclient = HAClient.getInstance();

        SignedLocationReport signedLocationReport = haclient.obtainLocationReport(1, 14L);
        assertNotNull(signedLocationReport);

        stopServer();
    }

    @Test
    public void obtainUsersAtLocation()
    {
        deleteServerReports();

        Client client15 = new Client(readUser(gridFileLocation, 15));
        UserClient userClient15 = new UserClient();
        UserServer userServer15 = new UserServer(client15);

        Client client19 = new Client(readUser(gridFileLocation, 19));
        UserClient userClient19 = new UserClient();
        UserServer userServer19 = new UserServer(client19);

        startServer(0);

        LocationReport locationReport15 = userClient15.requestLocationProofs(client15, 2L, 0);
        userClient15.submitLocationReport(client15, locationReport15);

        LocationReport locationReport19 = userClient19.requestLocationProofs(client19, 2L, 0);
        userClient19.submitLocationReport(client19, locationReport19);

        userServer15.stop();
        userServer19.stop();

        HAClient haclient = HAClient.getInstance();

        List<SignedLocationReport> signedLocationReportList = haclient.obtainUsersAtLocation(32L, 77L, 2L);
        assertNotNull(signedLocationReportList);
        assertEquals(signedLocationReportList.size(), 2);

        stopServer();
    }
}