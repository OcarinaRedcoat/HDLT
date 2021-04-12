package pt.tecnico.sec.hdlt.client.communication;

import com.google.protobuf.InvalidProtocolBufferException;
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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserClient {

    private static UserClient INSTANCE = null;
    private static final Logger logger = Logger.getLogger(UserClient.class.getName());

    private ArrayList<ClientServerGrpc.ClientServerBlockingStub> userStubs;
    private ArrayList<ManagedChannel> userChannels;

    private LocationServerGrpc.LocationServerBlockingStub serverStub;
    private ManagedChannel serverChannel;

    private UserClient(){
        userStubs = new ArrayList<>();
        userChannels = new ArrayList<>();
        serverStub = null;
        serverChannel = null;
    }

    public static UserClient getInstance(){
        if (INSTANCE == null)
            INSTANCE = new UserClient();

        return INSTANCE;
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

    public LocationReport requestLocationProofs(Long epoch){
        logger.info("Requesting Proof to user close by:");
        createCloseUsersChannels(Client.getInstance().getUser().getPositionWithEpoch(epoch).getCloseBy());

        LocationReport report = null;
        try {
            report = ClientBL.requestLocationProofs(epoch, userStubs);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        } finally {
            closeUserChannels();
        }

        return report;
    }

    public void submitLocationReport(LocationReport report){
        logger.info("Submitting Report:");
        createServerChannel("localhost", 50051);

        try {
            ClientBL.submitLocationReport(report, serverStub);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        } finally {
            closeServerChannel();
        }
    }

    public LocationReport obtainLocationReport(Long epoch){
        logger.info("Requesting report:");
        createServerChannel("localhost", 50051);

        LocationReport report = null;
        try {
            report = ClientBL.obtainLocationReport(epoch, serverStub);
            //TODO: print report
            System.out.println("I got the report Report!");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        } finally {
            closeServerChannel();
        }

        return report;
    }

}
