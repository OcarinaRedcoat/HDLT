package pt.tecnico.sec.hdlt.client.communication;

import io.grpc.stub.StreamObserver;
import org.json.simple.parser.ParseException;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.*;

import java.io.IOException;

class UserImpl extends ProofServerGrpc.ProofServerImplBase{

    //TODO: FAZER AS EXCEPCOES!!!!!
    @Override
    public void requestLocationProof(LocationProofBetweenClientsRequest req, StreamObserver<LocationProofBetweenClientsResponse> responseObserver){
        int requesterId = req.getUserId();
        long epoch = req.getEpoch();
        long requesterXPos = req.getRequesterX();
        long requesterYPos = req.getRequesterY();

        if(Client.getInstance().getUser().isCloseTo(requesterId, epoch)) {
            LocationBetweenClientsProof proof = LocationBetweenClientsProof
                    .newBuilder()
                    .setRequesterUserId(requesterId)
                    .setWitnessId(Client.getInstance().getUser().getId())
                    .setEpoch(epoch)
                    .setRequesterX(requesterXPos)
                    .setRequesterY(requesterYPos)
                    .build();

            LocationProofBetweenClientsResponse response = LocationProofBetweenClientsResponse.newBuilder().setProof(proof).build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            //todo: trow error message to client
        }
    }

}
