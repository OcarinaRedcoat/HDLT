package pt.tecnico.sec.hdlt.haclient;

import org.junit.Test;
import pt.tecnico.sec.hdlt.client.services.Server;
import pt.tecnico.sec.hdlt.client.services.UserClient;
import pt.tecnico.sec.hdlt.communication.LocationReport;
import pt.tecnico.sec.hdlt.communication.SignedLocationReport;
import pt.tecnico.sec.hdlt.entities.Client;
import pt.tecnico.sec.hdlt.haclient.communication.HAClient;
import pt.tecnico.sec.hdlt.haclient.ha.HA;
import pt.tecnico.sec.hdlt.server.LocationServer;
import pt.tecnico.sec.hdlt.server.bll.LocationBL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.*;
import static pt.tecnico.sec.hdlt.utils.IOUtils.readUser;

public class TestHA
{


    private static final String gridFileLocation = "../grids.output.json";

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
    public void obtainLocationReport()
    {

        //START CLIENTS
        Client client1 = new Client(readUser(gridFileLocation, 1), "client_1");
        UserClient userClient1 = new UserClient(client1);
        Client client6 = new Client(readUser(gridFileLocation, 6), "client_6");
        Server userServer6 = new Server(client6);
        Client client12 = new Client(readUser(gridFileLocation, 12), "client_12");
        Server userServer12 = new Server(client12);
        Client client19 = new Client(readUser(gridFileLocation, 19), "client_19");
        Server userServer19 = new Server(client19);


        //MAKE A REPORT
        LocationReport.Builder reportBuilder = userClient1.requestLocationProofs(24L, 2);

        //STOP CLIENT SERVERS
        userServer6.stop();
        userServer12.stop();
        userServer19.stop();

        //START HA
        HA myHa = new HA("ha_keystore_1");
        HAClient haClient = new HAClient(myHa);

        //START SERVERS
        int id = 1;
        deleteServerReports(id);
        LocationServer server1 = new LocationServer(id, new LocationBL(id, "server_" + id));
        id = 2;
        deleteServerReports(id);
        LocationServer server2 = new LocationServer(id, new LocationBL(id, "server_" + id));
        id = 3;
        deleteServerReports(id);
        LocationServer server3 = new LocationServer(id, new LocationBL(id, "server_" + id));
        id = 4;
        deleteServerReports(id);
        LocationServer server4 = new LocationServer(id, new LocationBL(id, "server_" + id));

        //SUBMIT A REPORT
        Boolean submitedReport = userClient1.submitLocationReport(reportBuilder);
        assertEquals(true, submitedReport);

        //GET A REPORT
        LocationReport locationReport = haClient.obtainLocationReport(1,24L);
        assertNotNull(locationReport);

        //STOP SERVERS
        server1.stop();
        server2.stop();
        server3.stop();
        server4.stop();

        userClient1.closeServerChannel();
        haClient.serverShutdown();
    }

//    TODO o F já não pode ser 0
    
    @Test
    public void obtainUsersAtLocation()
    {
        Client client15 = new Client(readUser(gridFileLocation, 15), "client_15");
        UserClient userClient15 = new UserClient(client15);
        Server userServer15 = new Server(client15);

        Client client19 = new Client(readUser(gridFileLocation, 19), "client_19");
        UserClient userClient19 = new UserClient(client19);
        Server userServer19 = new Server(client19);

        Client client6 = new Client(readUser(gridFileLocation, 6), "client_6");
        Server userServer6 = new Server(client6);
        Client client7 = new Client(readUser(gridFileLocation, 7), "client_7");
        Server userServer7 = new Server(client7);


        //MAKE A REPORT
        LocationReport.Builder reportBuilder15 = userClient15.requestLocationProofs(2L, 2);
        LocationReport.Builder reportBuilder19 = userClient19.requestLocationProofs(2L, 2);

        userServer6.stop();
        userServer7.stop();
        userServer19.stop();
        userServer15.stop();

        HA myHa = new HA("ha_keystore_1");
        HAClient haClient = new HAClient(myHa);

        //START SERVERS
        int id = 1;
        deleteServerReports(id);
        LocationServer server1 = new LocationServer(id, new LocationBL(id, "server_" + id));
        id = 2;
        deleteServerReports(id);
        LocationServer server2 = new LocationServer(id, new LocationBL(id, "server_" + id));
        id = 3;
        deleteServerReports(id);
        LocationServer server3 = new LocationServer(id, new LocationBL(id, "server_" + id));
        id = 4;
        deleteServerReports(id);
        LocationServer server4 = new LocationServer(id, new LocationBL(id, "server_" + id));

        Boolean submitedReport15 = userClient15.submitLocationReport(reportBuilder15);
        assertEquals(true, submitedReport15);
        Boolean submitedReport19 = userClient19.submitLocationReport(reportBuilder19);
        assertEquals(true, submitedReport19);

        List<SignedLocationReport> locationReportList = haClient.obtainUsersAtLocation(32,77,2L);
        assertNotNull(locationReportList);
        assertEquals(locationReportList.size(), 2);

        //STOP SERVERS
        server1.stop();
        server2.stop();
        server3.stop();
        server4.stop();

        userClient15.closeServerChannel();
        userClient19.closeServerChannel();
        haClient.serverShutdown();
    }
}
