package pt.tecnico.sec.hdlt.client.communication;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sec.hdlt.client.bll.ClientBL;
import pt.tecnico.sec.hdlt.entities.Client;
import pt.tecnico.sec.hdlt.communication.*;

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

public class UserClient {

    private static final Logger logger = Logger.getLogger(UserClient.class.getName());

    private ArrayList<ClientServerGrpc.ClientServerStub> userStubs;
    private ArrayList<ManagedChannel> userChannels;

    private ArrayList<LocationServerGrpc.LocationServerStub> serverStubs;
    private ArrayList<ManagedChannel> serverChannels;

    private ClientBL clientBL;

    public UserClient(){
        this.userStubs = new ArrayList<>();
        this.userChannels = new ArrayList<>();
        createServerStubs();
        this.clientBL = new ClientBL(serverStubs);
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

    private void closeUserChannels(){
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
            ArrayList<Long> witnessesId = client.getUser().getPositionWithEpoch(epoch).getCloseBy();
            createCloseUsersAsyncStubs(witnessesId);
            logger.info("Requesting Proof to user close by:");

            reportBuilder = clientBL.requestLocationProofs(userStubs, client, epoch, f, witnessesId);
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
        Boolean success = false;

        try {
            success = clientBL.submitLocationReport(client, reportBuilder);
            if(success){
                System.out.println("Submitted report successfully");
            }
        } catch (NoSuchAlgorithmException | SignatureException | InvalidAlgorithmParameterException | IOException |
                InvalidKeyException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException |
                CertificateException | InterruptedException e) {

            System.err.println("Something went wrong!");
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "server RPC failed: {0}:", e.getStatus().getDescription());
        }

        return success;
    }

    public LocationReport obtainLocationReport(Client client, Long epoch){
        LocationReport report = null;

        try {
            report = clientBL.obtainLocationReport(client, epoch);
            if(report != null){
                System.out.println("I got the report Report you wanted: ");
                System.out.println(report);
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchPaddingException |
                BadPaddingException | IllegalBlockSizeException | IOException |
                InvalidAlgorithmParameterException | CertificateException | InvalidParameterException |
                InterruptedException e) {

            System.err.println("Something went wrong!");
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "server RPC failed: {0}:", e.getStatus().getDescription());
        }

        return report;
    }

    public Proofs obtainMyProofs(Client client, List<Long> epochs){
        Proofs proofs = null;

        try {
            proofs = clientBL.ObtainMyProofs(client, epochs);
            if(proofs != null){
                System.out.println("I got the proofs you wanted: ");
                System.out.println(proofs);
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchPaddingException |
                BadPaddingException | IllegalBlockSizeException | InvalidKeySpecException | IOException |
                InvalidAlgorithmParameterException | CertificateException | InvalidParameterException |
                InterruptedException e) {

            System.err.println("Something went wrong!");
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "server RPC failed: {0}:", e.getStatus().getDescription());
        }

        return proofs;
    }

}
