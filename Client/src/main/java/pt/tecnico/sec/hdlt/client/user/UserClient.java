package pt.tecnico.sec.hdlt.client.user;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sec.hdlt.communication.LocationProofRequest;
import pt.tecnico.sec.hdlt.communication.LocationProofResponse;
import pt.tecnico.sec.hdlt.communication.LocationServerGrpc;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserClient {

    private static final Logger logger = Logger.getLogger(UserClient.class.getName());

    private ArrayList<LocationServerGrpc.LocationServerBlockingStub> stubList;

    private ArrayList<Channel> channels;

    public UserClient(User user, ArrayList<User> closeUsers){
        stubList = new ArrayList<>();
        channels = new ArrayList<>();

        for (User usr: closeUsers) {
            String target = usr.getHost() + ":" + usr.getPort();
            ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                    .usePlaintext()
                    .build();
            channels.add(channel);
            LocationServerGrpc.LocationServerBlockingStub blockingStub = LocationServerGrpc.newBlockingStub(channel);
            stubList.add(blockingStub);
        }
    }


    public void requestLocationProof(User user, int epoch){
        logger.info("Requesting Proof to user close by:");
        LocationProofRequest request = LocationProofRequest.newBuilder().setUserId(String.valueOf(user.getId()))
                .setEpoch(epoch).setRequesterX(user.getX_position()).setRequesterY(user.getY_position()).build();
        ArrayList<LocationProofResponse> responses = new ArrayList<>();
        try{
            for (LocationServerGrpc.LocationServerBlockingStub stub : stubList) {
                 LocationProofResponse response = stub.requestLocationProof(request);
                 responses.add(response);
            }
        } catch ( StatusRuntimeException e) {
        logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

}
