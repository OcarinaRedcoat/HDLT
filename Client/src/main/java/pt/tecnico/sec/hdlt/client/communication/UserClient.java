package pt.tecnico.sec.hdlt.client.communication;

import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sec.hdlt.User;
import pt.tecnico.sec.hdlt.client.bll.ClientBL;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserClient {

    private static final int serverPort = 50051;
    private static final String serverAddress = "localhost";

    private static final Logger logger = Logger.getLogger(UserClient.class.getName());

    private ArrayList<ClientServerGrpc.ClientServerBlockingStub> userStubs;
    private ArrayList<ManagedChannel> userChannels;

    private LocationServerGrpc.LocationServerBlockingStub serverStub;
    private ManagedChannel serverChannel;

    public UserClient(){
        userStubs = new ArrayList<>();
        userChannels = new ArrayList<>();
        serverStub = null;
        serverChannel = null;
    }

    private void createCloseUsersChannels(ArrayList<Long> closeUsers){
        userStubs = new ArrayList<>();
        userChannels = new ArrayList<>();
        for (Long closeUserId: closeUsers) {
            String target = "localhost:" + (10000 + closeUserId);
            ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                    .usePlaintext()
                    .build();
            userChannels.add(channel);
            ClientServerGrpc.ClientServerBlockingStub blockingStub = ClientServerGrpc.newBlockingStub(channel);
            userStubs.add(blockingStub);
        }
    }

    private void createServerChannel(String host, int port){
        String target = host + ":" + port;
        serverChannel = ManagedChannelBuilder.forTarget(target)
                    .usePlaintext()
                    .build();
        serverStub = LocationServerGrpc.newBlockingStub(serverChannel);
    }

    public void closeUserChannels(){
        for (ManagedChannel channel : userChannels) {
            channel.shutdownNow();
        }
        userChannels = new ArrayList<>();
        userStubs = new ArrayList<>();
    }

    public void closeServerChannel(){
        serverChannel.shutdownNow();
        serverChannel = null;
        serverStub = null;
    }

    public LocationReport requestLocationProofs(Client client, Long epoch, int f){
        LocationReport report = null;
        try {
            createCloseUsersChannels(client.getUser().getPositionWithEpoch(epoch).getCloseBy());
            logger.info("Requesting Proof to user close by:");

            report = ClientBL.requestLocationProofs(client, epoch, f, userStubs);
            System.out.println("Got the location proofs");
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException |
                IOException | CertificateException e) {

            System.err.println("Something went wrong!");
        } catch (InvalidParameterException e) {
            System.err.println(e.getMessage());
        } finally {
            closeUserChannels();
        }

        return report;
    }

    public Boolean submitLocationReport(Client client, LocationReport report){
        if(report == null){
            System.err.println("Invalid Report!");
            return false;
        }

        logger.info("Submitting Report:");
        createServerChannel(serverAddress, serverPort);

        try {
            ClientBL.submitLocationReport(client, report, serverStub);
            System.out.println("Submitted report successfully");
            return true;
        } catch (NoSuchAlgorithmException | SignatureException | InvalidAlgorithmParameterException | IOException |
                InvalidKeyException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException |
                InvalidKeySpecException | CertificateException e) {

            System.err.println("Something went wrong!");
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "server RPC failed: {0}:", e.getStatus().getDescription());
        } finally {
            closeServerChannel();
        }
        return false;
    }

    public LocationReport obtainLocationReport(Client client, Long epoch){
        logger.info("Requesting report:");
        createServerChannel(serverAddress, serverPort);

        LocationReport report = null;
        try {
            report = ClientBL.obtainLocationReport(client, epoch, serverStub);
            System.out.println("I got the report Report you wanted: ");
            System.out.println(report);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchPaddingException |
                BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException | IOException |
                InvalidAlgorithmParameterException | CertificateException e) {

            System.err.println("Something went wrong!");
        } catch (InvalidParameterException e) {
            System.err.println(e.getMessage());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "server RPC failed: {0}:", e.getStatus().getDescription());
        } finally {
            closeServerChannel();
        }

        return report;
    }

}
