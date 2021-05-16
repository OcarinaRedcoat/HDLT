package pt.tecnico.sec.hdlt.client;

import org.junit.Test;
import pt.tecnico.sec.hdlt.client.communication.UserClient;
import pt.tecnico.sec.hdlt.client.communication.UserServer;
import pt.tecnico.sec.hdlt.entities.Client;
import pt.tecnico.sec.hdlt.communication.LocationReport;
import pt.tecnico.sec.hdlt.server.LocationServer;
import pt.tecnico.sec.hdlt.server.bll.LocationBL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;
import static pt.tecnico.sec.hdlt.utils.IOUtils.readUser;

public class TestClient2ServerCommunication {

    private static final String gridFileLocation = "../grids.output.json";

    private LocationBL createServerLocationBl(int serverId){
        try {
            return new LocationBL(serverId, "server_" + serverId);
        } catch (Exception e) {
            fail();
        }
        return null;
    }

    private void startServer(int id, LocationServer server, LocationBL locationBL){
        try {
            server.start(id, locationBL);
        } catch (Exception e) {
            fail();
        }
    }

    private void stopServer(LocationServer server, LocationBL locationBL){
        try {
            server.stop();
            locationBL.terminateMessageWriteQueue();
            locationBL.terminateNonceWriteQueue();
        } catch (InterruptedException e) {
            fail();
        }
    }

    private void deleteServerReports(int serverId){
        Path fileToDeletePath = Paths.get("../Server/src/main/resources/server_" + serverId + ".txt");
        Path fileToDeletePath2 = Paths.get("../Server/src/main/resources/server_" + serverId + "_nonce.txt");
        try {
            Files.deleteIfExists(fileToDeletePath);
            Files.deleteIfExists(fileToDeletePath2);
        } catch (IOException e) {
            fail();
        }
    }

    @Test
    public void submitReportAndGetReport()
    {
//        //START CLIENTS
//        Client client1 = new Client(readUser(gridFileLocation, 1), "client_1");
//        UserClient userClient1 = new UserClient(client1);
//        Client client6 = new Client(readUser(gridFileLocation, 6), "client_6");
//        UserServer userServer6 = new UserServer(client6);
//        Client client12 = new Client(readUser(gridFileLocation, 12), "client_12");
//        UserServer userServer12 = new UserServer(client12);
//        Client client19 = new Client(readUser(gridFileLocation, 19), "client_19");
//        UserServer userServer19 = new UserServer(client19);
//
//        //MAKE A REPORT
//        LocationReport.Builder reportBuilder = userClient1.requestLocationProofs(24L, 2);
//
//        //STOP CLIENT SERVERS
//        userServer6.stop();
//        userServer12.stop();
//        userServer19.stop();
//
//        //START SERVERS
//        int id = 1;
//        deleteServerReports(id);
//        LocationServer server1 = new LocationServer();
//        LocationBL locationBL1 = createServerLocationBl(id);
//        startServer(id, server1, locationBL1);
//        id = 2;
//        deleteServerReports(id);
//        LocationServer server2 = new LocationServer();
//        LocationBL locationBL2 = createServerLocationBl(id);
//        startServer(id, server2, locationBL2);
//        id = 3;
//        deleteServerReports(id);
//        LocationServer server3 = new LocationServer();
//        LocationBL locationBL3 = createServerLocationBl(id);
//        startServer(id, server3, locationBL3);
//        id = 4;
//        deleteServerReports(id);
//        LocationServer server4 = new LocationServer();
//        LocationBL locationBL4 = createServerLocationBl(id);
//        startServer(id, server4, locationBL4);
//
//        //SUBMIT A REPORT
//        Boolean submitedReport = userClient1.submitLocationReport(reportBuilder);
//        assertEquals(true, submitedReport);
//
//        //GET A REPORT
//        LocationReport locationReport = userClient1.obtainLocationReport(24L);
//        assertNotNull(locationReport);
//
//        //STOP SERVERS
//        stopServer(server1, locationBL1);
//        stopServer(server2, locationBL2);
//        stopServer(server3, locationBL3);
//        stopServer(server4, locationBL4);
    }
}
