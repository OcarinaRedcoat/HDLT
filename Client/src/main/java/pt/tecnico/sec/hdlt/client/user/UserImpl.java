package pt.tecnico.sec.hdlt.client.user;

import io.grpc.stub.StreamObserver;
import org.json.simple.parser.ParseException;
import pt.tecnico.sec.hdlt.client.utils.FileUtils;
import pt.tecnico.sec.hdlt.communication.LocationProof;
import pt.tecnico.sec.hdlt.communication.LocationProofRequest;
import pt.tecnico.sec.hdlt.communication.LocationProofResponse;
import pt.tecnico.sec.hdlt.communication.LocationServerGrpc;

import java.io.IOException;

class UserImpl extends LocationServerGrpc.LocationServerImplBase{

    public UserImpl(){

    }

    //TODO: FAZER AS EXCEPCOES!!!!!
    @Override
    public void requestLocationProof(LocationProofRequest req, StreamObserver<LocationProofResponse> responseObserver){
        int requesterId = req.getUserId();
        long currentEpoch = req.getEpoch(); //todo não sei se validamos aqui a epoch que recebemos ou so validamos no server
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
