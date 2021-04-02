package pt.tecnico.sec.hdlt.client.communication;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.client.user.User;
import pt.tecnico.sec.hdlt.communication.LocationProofRequest;
import pt.tecnico.sec.hdlt.communication.LocationProofResponse;
import pt.tecnico.sec.hdlt.communication.LocationServerGrpc;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserClient {

    private static UserClient INSTANCE = null;
    private static final Logger logger = Logger.getLogger(UserClient.class.getName());

    private ArrayList<LocationServerGrpc.LocationServerBlockingStub> stubList;
    private ArrayList<ManagedChannel> channels;

    private UserClient(){
        stubList = new ArrayList<>();
        channels = new ArrayList<>();
    }

    public static UserClient getInstance(){
        if (INSTANCE == null)
            INSTANCE = new UserClient();

        return INSTANCE;
    }

    private void createCloseUsersChannels(ArrayList<Long> closeUsers){
        for (Long closeUserId: closeUsers) {
            //TODO: mudar para nao estar estatico, não se se é preciso por cause de bluthoth
            String target = "localhost:" + (10000 + closeUserId);
            ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                    .usePlaintext()
                    .build();
            channels.add(channel);
            LocationServerGrpc.LocationServerBlockingStub blockingStub = LocationServerGrpc.newBlockingStub(channel);
            stubList.add(blockingStub);
        }
    }

    public void closeUserChannels(){
        for (ManagedChannel channel : channels) {
            channel.shutdownNow();
        }
    }

    //TODO
    public void requestLocationProof(Long epoch){
        User user = Client.getInstance().getUser();
        createCloseUsersChannels(user.getPositionWithEpoch(epoch).getCloseBy());

        logger.info("Requesting Proof to user close by:");
        LocationProofRequest request = LocationProofRequest
                .newBuilder()
                .setUserId(user.getId())
                .setEpoch(epoch)
                .setRequesterX(user.getPositionWithEpoch(epoch).getxPos())
                .setRequesterY(user.getPositionWithEpoch(epoch).getyPos())
                .build();
        ArrayList<LocationProofResponse> responses = new ArrayList<>();
        for (LocationServerGrpc.LocationServerBlockingStub stub : stubList) {
            try{
                LocationProofResponse response = stub.requestLocationProof(request);
                responses.add(response);
                closeUserChannels();
            } catch ( StatusRuntimeException e) {
                logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            }
        }

        //TODO: Fazer alguma coisa com os response (dar return maybe e depois enviar para o servidor)

    }

    //TODO
    public void submitLocationReport(int userId, Long ep/*, Report report*/){

    }

    //TODO
    public void obtainLocationReport(int userId, Long ep){

    }

}
