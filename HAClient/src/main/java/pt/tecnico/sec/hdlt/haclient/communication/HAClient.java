package pt.tecnico.sec.hdlt.haclient.communication;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sec.hdlt.communication.LocationReport;
import pt.tecnico.sec.hdlt.communication.LocationServerGrpc;
import pt.tecnico.sec.hdlt.communication.SignedLocationReport;
import pt.tecnico.sec.hdlt.haclient.bll.HABL;
import pt.tecnico.sec.hdlt.haclient.ha.HA;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pt.tecnico.sec.hdlt.utils.GeneralUtils.*;

public class HAClient {

    private static HAClient INSTANCE = null;
    private static HA ha = null;


    private ArrayList<LocationServerGrpc.LocationServerStub> serverStubs;
    private ArrayList<ManagedChannel> serverChannels;


//    private LocationServerGrpc.LocationServerBlockingStub serverStub;
//    private ManagedChannel serverChannel;
    private static final Logger logger = Logger.getLogger(HAClient.class.getName());

    private HABL haBL;

     public HAClient(HA ha) {
        createServerStubs();
        this.haBL = new HABL(ha, serverStubs);
        //HA.getInstance();
    }

    private void createServerStubs(){
        serverStubs = new ArrayList<>();
        serverChannels = new ArrayList<>();
        for (int i = 0; i < N_SERVERS; i++) {
            String target = SERVER_HOST + ":" + (SERVER_START_PORT + i);
            ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                    .usePlaintext()
                    .build();
            serverChannels.add(channel);
            LocationServerGrpc.LocationServerStub stub = LocationServerGrpc.newStub(channel);
            serverStubs.add(stub);
        }
    }

    public void serverShutdown(){
         for (ManagedChannel channel : serverChannels){
             channel.shutdownNow();
         }

         serverChannels = new ArrayList<>();
         serverStubs = new ArrayList<>();
    }



















    public LocationReport obtainLocationReport(int userId, Long epoch){
        logger.info("Obtain Location Report:");

        LocationReport report = null;

        try {
            report = haBL.obtainLocationReport(userId, epoch);
            if (report != null){
                System.out.println("I got the Report that you wanted for user " + userId);
                System.out.println(report);
            } else {
                System.out.println("I was unsuccessful at getting the Report for user " + userId);
            }
        } catch (NoSuchAlgorithmException | SignatureException | NoSuchPaddingException | BadPaddingException |
                InvalidKeyException | IllegalBlockSizeException | InvalidKeySpecException | InvalidParameterException
                | InvalidAlgorithmParameterException | IOException | CertificateException e) {
            //System.err.println(e.getMessage());
            System.err.println("Something went wrong!");
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus().getDescription());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return report;
    }
    /* Params: pos, ep .....
     * Specification: returns a list of users that were at position pos at epoch ep
     */
    public List<LocationReport> obtainUsersAtLocation(long x, long y, long ep) {
        logger.info("Obtain Users At Location:");

        List<LocationReport> listReport;

        try{
            listReport = haBL.obtainUsersAtLocation(x , y , ep);
            return listReport;
        }catch (NoSuchAlgorithmException | SignatureException | NoSuchPaddingException | BadPaddingException |
                InvalidKeyException | IllegalBlockSizeException | InvalidKeySpecException | InvalidParameterException
                | InvalidAlgorithmParameterException | IOException | CertificateException e) {
            System.err.println(e.getMessage());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus().getDescription());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
