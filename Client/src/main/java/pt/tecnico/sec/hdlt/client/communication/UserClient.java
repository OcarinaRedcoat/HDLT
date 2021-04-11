package pt.tecnico.sec.hdlt.client.communication;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.client.user.User;
import pt.tecnico.sec.hdlt.communication.*;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserClient {

    private static UserClient INSTANCE = null;
    private static final Logger logger = Logger.getLogger(UserClient.class.getName());

    private ArrayList<ProofServerGrpc.ProofServerBlockingStub> userStubs;
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
            ProofServerGrpc.ProofServerBlockingStub blockingStub = ProofServerGrpc.newBlockingStub(channel);
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

    public ArrayList<LocationProofBetweenClientsResponse> requestLocationProof(Long epoch){
        User user = Client.getInstance().getUser();
        createCloseUsersChannels(user.getPositionWithEpoch(epoch).getCloseBy());

        logger.info("Requesting Proof to user close by:");
        LocationProofBetweenClientsRequest request = LocationProofBetweenClientsRequest
                .newBuilder()
                .setUserId(user.getId())
                .setEpoch(epoch)
                .setRequesterX(user.getPositionWithEpoch(epoch).getxPos())
                .setRequesterY(user.getPositionWithEpoch(epoch).getyPos())
                .build();
        ArrayList<LocationProofBetweenClientsResponse> responses = new ArrayList<>();
        for (ProofServerGrpc.ProofServerBlockingStub stub : userStubs) {
            try{
                LocationProofBetweenClientsResponse response = stub.requestLocationProof(request);
                responses.add(response);
                closeUserChannels();
            } catch ( StatusRuntimeException e) {
                logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            }
        }

        return responses;
    }

    //TODO
    public void submitLocationReport(Long epoch){

    }

    public SubmitLocationReportResponse obtainLocationReport(Long epoch){
        User user = Client.getInstance().getUser();
        createServerChannel("localhost", 11000);

        logger.info("Requesting Proof to user close by:");
        SubmitLocationReportRequest request = SubmitLocationReportRequest
                .newBuilder()
                .setUserId(user.getId())
//                .setEpoch(epoch)
                .build();
        SubmitLocationReportResponse response = null;
        try{
            response = serverStub.submitLocationReport(request);
            closeServerChannel();
        } catch ( StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }

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
