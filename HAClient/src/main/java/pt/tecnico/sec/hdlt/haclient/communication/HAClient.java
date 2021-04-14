package pt.tecnico.sec.hdlt.haclient.communication;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sec.hdlt.communication.LocationReport;
import pt.tecnico.sec.hdlt.communication.LocationServerGrpc;
import pt.tecnico.sec.hdlt.haclient.bll.HABL;
import pt.tecnico.sec.hdlt.haclient.ha.HA;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HAClient {

    private static HAClient INSTANCE = null;
    private static HA ha = null;

    private LocationServerGrpc.LocationServerBlockingStub serverStub;
    private ManagedChannel serverChannel;
    private static final Logger logger = Logger.getLogger(HAClient.class.getName());

    private HAClient(String host, int port) {
        createServerChannel(host, port);
        HA.getInstance();
    }


    private void createServerChannel(String host, int port){
        String target = host + ":" + String.valueOf(port);
        serverChannel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        serverStub = LocationServerGrpc.newBlockingStub(serverChannel);
    }

    public void serverShutdown(){
        serverChannel.shutdownNow();
    }

    public static HAClient getInstance() {
        if (INSTANCE == null)
            INSTANCE = new HAClient("localhost", 50051);
        return INSTANCE;
    }

    public void obtainLocationReport(int userId, Long epoch){
        logger.info("Obtain Location Report:");

        LocationReport report = null;

        try{
            report = HABL.obtainLocationReport(userId, epoch, serverStub);
        } catch (NoSuchAlgorithmException | SignatureException | NoSuchPaddingException | BadPaddingException |
                InvalidKeyException | IllegalBlockSizeException | InvalidKeySpecException | InvalidParameterException
                | InvalidAlgorithmParameterException | IOException e) {
            e.printStackTrace();
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus().getDescription());
        }

        System.out.println(report);

    }
    /* Params: pos, ep .....
     * Specification: returns a list of users that were at position pos at epoch ep
     */
    public void obtainUsersAtLocation(long x, long y, long ep) {
        logger.info("Obtain Users At Location:");

        List<LocationReport> listReport = null;

        try{
            listReport = HABL.obtainUsersAtLocation(x , y , ep, serverStub);
        }catch (NoSuchAlgorithmException | SignatureException | NoSuchPaddingException | BadPaddingException |
                InvalidKeyException | IllegalBlockSizeException | InvalidKeySpecException | InvalidParameterException
                | InvalidAlgorithmParameterException | IOException e) {
            e.printStackTrace();
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus().getDescription());
        }

        for (LocationReport report: listReport) {
            System.out.println(report);
        }
    }
}
