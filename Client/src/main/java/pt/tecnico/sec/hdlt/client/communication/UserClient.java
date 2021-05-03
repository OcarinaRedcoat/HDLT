package pt.tecnico.sec.hdlt.client.communication;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
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

import static pt.tecnico.sec.hdlt.GeneralUtils.*;

public class UserClient {

    private static final Logger logger = Logger.getLogger(UserClient.class.getName());

    private ArrayList<ClientServerGrpc.ClientServerStub> userStubs;
    private ArrayList<ManagedChannel> userChannels;

    private ArrayList<LocationServerGrpc.LocationServerStub> serverStubs;
    private ArrayList<ManagedChannel> serverChannels;

    public UserClient(){
        userStubs = new ArrayList<>();
        userChannels = new ArrayList<>();
        serverStubs = new ArrayList<>();
        serverChannels = new ArrayList<>();
    }

    private void createCloseUsersAsyncStubs(ArrayList<Long> closeUsers){
        userStubs = new ArrayList<>();
        userChannels = new ArrayList<>();
        for (Long closeUserId: closeUsers) {
            String target = "localhost:" + (10000 + closeUserId);
            ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                    .usePlaintext()
                    .build();
            userChannels.add(channel);
            ClientServerGrpc.ClientServerStub stub = ClientServerGrpc.newStub(channel);
            userStubs.add(stub);
        }
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

    public void closeUserChannels(){
        for (ManagedChannel channel : userChannels) {
            channel.shutdownNow();
        }
        userChannels = new ArrayList<>();
        userStubs = new ArrayList<>();
    }

    public void closeServerChannel(){

        for (ManagedChannel channel : serverChannels) {
            channel.shutdownNow();
        }
        serverChannels = new ArrayList<>();
        serverStubs = new ArrayList<>();
    }

    public LocationReport.Builder requestLocationProofs(Client client, Long epoch, int f){
        LocationReport.Builder reportBuilder = null;
        try {
            createCloseUsersAsyncStubs(client.getUser().getPositionWithEpoch(epoch).getCloseBy());
            logger.info("Requesting Proof to user close by:");

            reportBuilder = ClientBL.requestLocationProofs(client, epoch, f, userStubs);
            System.out.println("Got the location proofs");
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InvalidKeySpecException |
                IOException | CertificateException | InterruptedException e) {

            System.err.println("Something went wrong!");
        } catch (InvalidParameterException e) {
            System.err.println(e.getMessage());
        }  finally {
            closeUserChannels();
        }

        return reportBuilder;
    }

    public Boolean submitLocationReport(Client client, LocationReport.Builder reportBuilder){
        createServerStubs();

        try {
            ClientBL.submitLocationReport(client, reportBuilder, serverStubs);
            System.out.println("Submitted report successfully");
            return true;
        } catch (NoSuchAlgorithmException | SignatureException | InvalidAlgorithmParameterException | IOException |
                InvalidKeyException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException |
                InvalidKeySpecException | CertificateException | InterruptedException e) {

            System.err.println("Something went wrong!");
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "server RPC failed: {0}:", e.getStatus().getDescription());
        } finally {
            closeServerChannel();
        }
        return false;
    }

    public LocationReport obtainLocationReport(Client client, Long epoch){
        createServerStubs();

        LocationReport report = null;
        try {
            report = ClientBL.obtainLocationReport(client, epoch, serverStubs);
            System.out.println("I got the report Report you wanted: ");
            System.out.println(report);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchPaddingException |
                BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException | IOException |
                InvalidAlgorithmParameterException | CertificateException | InvalidParameterException e) {

            System.err.println("Something went wrong!");
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "server RPC failed: {0}:", e.getStatus().getDescription());
        } finally {
            closeServerChannel();
        }

        return report;
    }

}
