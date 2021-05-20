package pt.tecnico.sec.hdlt.client;

import org.junit.Test;
import pt.tecnico.sec.hdlt.client.services.Server;
import pt.tecnico.sec.hdlt.client.services.UserClient;
import pt.tecnico.sec.hdlt.communication.LocationReport;
import pt.tecnico.sec.hdlt.communication.Proofs;
import pt.tecnico.sec.hdlt.entities.Client;
import pt.tecnico.sec.hdlt.entities.User;
import pt.tecnico.sec.hdlt.server.LocationServer;
import pt.tecnico.sec.hdlt.server.bll.LocationBL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.*;
import static pt.tecnico.sec.hdlt.utils.IOUtils.readUser;

public class TestClient2ServerCommunication {

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
    public void submitReportGetReportGetMyProofsTest()
    {
        //START CLIENTS
        Client client1 = new Client(readUser(gridFileLocation, 1), "client_1");
        UserClient userClient1 = new UserClient(client1);
        Client client6 = new Client(readUser(gridFileLocation, 6), "client_6");
        UserClient userClient6 = new UserClient(client6);
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
        LocationReport locationReport = userClient1.obtainLocationReport(24L);
        assertNotNull(locationReport);

        //Get my proofs
        List<Long> aux = new ArrayList<>();
        aux.add(24L);
        Proofs proofs = userClient6.obtainMyProofs(aux);
        assertNotNull(proofs);

        //STOP SERVERS
        userClient1.closeServerChannel();
        userClient6.closeServerChannel();
        server1.stop();
        server2.stop();
        server3.stop();
        server4.stop();
    }
}
