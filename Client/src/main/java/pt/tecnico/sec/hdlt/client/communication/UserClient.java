package pt.tecnico.sec.hdlt.client.communication;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sec.hdlt.User;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.*;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pt.tecnico.sec.hdlt.FileUtils.*;
import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.sign;

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
            //TODO: mudar para nao estar estatico, não se se é preciso por cause de bluetooth
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

    public LocationReport requestLocationProofs(Long epoch){
        User user = Client.getInstance().getUser();
        createCloseUsersChannels(user.getPositionWithEpoch(epoch).getCloseBy());

        logger.info("Requesting Proof to user close by:");
        Position position = Position
                .newBuilder()
                .setX(user.getPositionWithEpoch(epoch).getxPos())
                .setY(user.getPositionWithEpoch(epoch).getyPos())
                .build();

        LocationInformation request = LocationInformation
                .newBuilder()
                .setUserId(user.getId())
                .setEpoch(epoch)
                .setPosition(position)
                .build();

        byte[] signature = new byte[0];
        try {
            signature = sign(request.toByteArray(), Client.getInstance().getPrivKey());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }

        LocationReport.Builder reportBuilder = LocationReport
                .newBuilder()
                .setLocationInformationSignature(ByteString.copyFrom(signature))
                .setLocationInformation(request);

        for (ClientServerGrpc.ClientServerBlockingStub stub : userStubs) {
            try{
                SignedLocationProof response = stub.requestLocationProof(request);
                reportBuilder.addLocationProof(response);
                closeUserChannels();
            } catch ( StatusRuntimeException e) {
                logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            }
        }

        return reportBuilder.build();
    }

    public void submitLocationReport(LocationReport report){
        User user = Client.getInstance().getUser();
        createServerChannel("localhost", 50051); //TODO

        logger.info("Submitting Report:");
        SubmitLocationReportRequest request = SubmitLocationReportRequest
                .newBuilder()
                .setUserId(user.getId())
                //TODO: .setEncryptedSignedLocationReport(report)
                .build();
        SubmitLocationReportResponse response = null;
        try{
            response = serverStub.submitLocationReport(request);
            //TODO: do something with response?
            closeServerChannel();
        } catch ( StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

    public ObtainLocationReportResponse obtainLocationReport(Long epoch){
        User user = Client.getInstance().getUser();
        createServerChannel("localhost", 50051); //TODO

        logger.info("Requesting report:");

        LocationQuery locationQuery = LocationQuery
                .newBuilder()
                .setUserId(user.getId())
                .setEpoch(epoch)
                .build();

        byte[] signature = new byte[0];
        try {
            signature = sign(locationQuery.toByteArray(), Client.getInstance().getPrivKey());
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
        }

        SignedLocationQuery signedLocationQuery = SignedLocationQuery
                .newBuilder()
                .setLocationQuery(locationQuery)
                .setSignature(ByteString.copyFrom(signature))
                .build();

        ObtainLocationReportRequest request = ObtainLocationReportRequest
                .newBuilder()
                //TODO: .setEncryptedSignedLocationQuery()
                .build();

        ObtainLocationReportResponse response = null;
        try{
            response = serverStub.obtainLocationReport(request);
            closeServerChannel();
        } catch ( StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }

        //TODO: decrypt InformationLocation
        //response.getEncryptedLocationInformation()

        return response;
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

}
