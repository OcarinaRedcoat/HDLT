package pt.tecnico.sec.hdlt.client.services;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sec.hdlt.client.bll.RequestBL;
import pt.tecnico.sec.hdlt.entities.Client;
import pt.tecnico.sec.hdlt.communication.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pt.tecnico.sec.hdlt.utils.GeneralUtils.*;

public class UserClient {

    private static final Logger logger = Logger.getLogger(UserClient.class.getName());

    private ArrayList<ClientToClientGrpc.ClientToClientStub> userStubs;
    private ArrayList<ManagedChannel> userChannels;

    private ArrayList<LocationServerGrpc.LocationServerStub> serverStubs;
    private ArrayList<ManagedChannel> serverChannels;

    private RequestBL requestBL;
    private Client client;

    public UserClient(Client client){
        this.userStubs = new ArrayList<>();
        this.userChannels = new ArrayList<>();
        createServerStubs();
        this.requestBL = new RequestBL(client, serverStubs);
        this.client = client;
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
            ClientToClientGrpc.ClientToClientStub stub = ClientToClientGrpc.newStub(channel);
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
            try {
                channel.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        userChannels = new ArrayList<>();
        userStubs = new ArrayList<>();
    }

    public void closeServerChannel(){
        for (ManagedChannel channel : serverChannels) {
            try {
                channel.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        serverChannels = new ArrayList<>();
        serverStubs = new ArrayList<>();
    }

    public LocationReport.Builder requestLocationProofs(Long epoch, int f){
        LocationReport.Builder reportBuilder = null;
        try {
            ArrayList<Long> witnessesId = client.getUser().getPositionWithEpoch(epoch).getCloseBy();
            createCloseUsersAsyncStubs(witnessesId);
            System.out.println("Requesting Proof to user close by:");
            reportBuilder = requestBL.requestLocationProofs(userStubs, epoch, f, witnessesId);
            System.out.println("Got the location proofs");
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | InterruptedException |
                InvalidParameterException e) {

            System.err.println("Something went wrong! Error: " + e.getMessage());
        }  finally {
            closeUserChannels();
        }

        return reportBuilder;
    }

    public Boolean submitLocationReport(LocationReport.Builder reportBuilder){
        LocationReport locationReport;

        try {
            System.out.println("Submitting location report:");
            locationReport = requestBL.submitLocationReport(reportBuilder);
            if(locationReport != null){
                System.out.println("Submitted report successfully");
                return true;
            } else {
                System.err.println("No enough server responses for a quorum. This can only happen if " +
                        "there where more crashes or byzantine servers than supported, or the client crashed");
            }
        } catch (NoSuchAlgorithmException | SignatureException | InvalidAlgorithmParameterException | IOException |
                InvalidKeyException | BadPaddingException | NoSuchPaddingException | IllegalBlockSizeException |
                CertificateException | InterruptedException e) {

            System.err.println("Something went wrong!");
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "server RPC failed: {0}:", e.getStatus().getDescription());
        }

        return false;
    }

    public LocationReport obtainLocationReport(Long epoch){
        LocationReport report = null;

        try {
            System.out.println("Obtaining location report:");
            report = requestBL.obtainLocationReport(epoch);
            if(report != null){
                System.out.println("I got the report Report you wanted: ");
                System.out.println(report);
            } else {
                System.out.println("I was unsuccessful at getting the report (Probably didn't get a quorum)");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchPaddingException |
                BadPaddingException | IllegalBlockSizeException | IOException | InvalidAlgorithmParameterException |
                CertificateException | InvalidParameterException | InterruptedException e) {

            System.err.println("Something went wrong!");
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "server RPC failed: {0}:", e.getStatus().getDescription());
        }

        return report;
    }

    public Proofs obtainMyProofs(List<Long> epochs){
        Proofs proofs = null;

        try {
            System.out.println("Obtaining my proofs:");
            proofs = requestBL.ObtainMyProofs(epochs);
            if(proofs != null){
                System.out.println("I got the proofs you wanted: ");
                System.out.println(proofs);
            } else {
                System.out.println("I was unsuccessful at getting the proofs (Probably didn't get a quorum)");
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | NoSuchPaddingException |
                BadPaddingException | IllegalBlockSizeException | IOException | InvalidAlgorithmParameterException |
                CertificateException | InvalidParameterException | InterruptedException e) {

            System.err.println("Something went wrong!");
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "server RPC failed: {0}:", e.getStatus().getDescription());
        }

        return proofs;
    }

}
