package pt.tecnico.sec.hdlt.server;

import org.junit.Test;
import pt.tecnico.sec.hdlt.server.bll.LocationBL;

import static org.junit.Assert.fail;

public class TestServer {


    @Test
    public void testIncorrectNumberOfProofs() {
        LocationServer locationServer = null;
        LocationBL locationBL = null;

        try {
            locationServer = new LocationServer();
            locationBL = new LocationBL(1, 2);
            locationServer.start(locationBL);



            locationServer.stop();
            locationBL.terminateWriteQueue();
        } catch (Exception e) {
            fail();
        }
    }
}
