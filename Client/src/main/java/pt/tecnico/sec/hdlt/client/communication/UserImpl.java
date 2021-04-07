package pt.tecnico.sec.hdlt.client.communication;

import io.grpc.stub.StreamObserver;
import org.json.simple.parser.ParseException;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.client.user.Position;
import pt.tecnico.sec.hdlt.client.user.User;
import pt.tecnico.sec.hdlt.client.utils.FileUtils;
import pt.tecnico.sec.hdlt.communication.*;

import java.io.IOException;

class UserImpl extends LocationServerGrpc.LocationServerImplBase{

    //TODO: FAZER AS EXCEPCOES!!!!!
    @Override
    public void requestLocationProof(LocationProofRequest req, StreamObserver<LocationProofResponse> responseObserver){
        int requesterId = req.getUserId();
        long currentEpoch = req.getEpoch();
        long requesterXPos = req.getRequesterX();
        long requesterYPos = req.getRequesterY();

        if(Client.getInstance().getUser().isCloseTo(requesterId, currentEpoch)) {
            try{
                User user = FileUtils.getInstance().parseGridUser(requesterId);
                Position position = user.getPositionWithEpoch(currentEpoch);

                LocationProof proof = LocationProof
                        .newBuilder()
                        .setRequesterX(requesterXPos)
                        .setRequesterY(requesterYPos)
                        .setRequestedX(position.getxPos())
                        .setRequesterY(position.getyPos())
                        .setRequesterUserId(requesterId)
                        .setRequestedUserId(user.getId())
                        .setEpoch(currentEpoch)
                        .build();

                LocationProofResponse response = LocationProofResponse.newBuilder().setProof(proof).build();

                responseObserver.onNext(response);
                responseObserver.onCompleted();
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
        }
    }

}
