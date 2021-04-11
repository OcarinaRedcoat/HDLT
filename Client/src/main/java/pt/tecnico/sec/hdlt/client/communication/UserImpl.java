package pt.tecnico.sec.hdlt.client.communication;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.*;


class UserImpl extends ClientServerGrpc.ClientServerImplBase{

    @Override
    public void requestLocationProof(LocationInformation req, StreamObserver<SignedLocationProof> responseObserver){
        int requesterId = req.getUserId();
        long epoch = req.getEpoch();
        long requesterXPos = req.getPosition().getX();
        long requesterYPos = req.getPosition().getY();

        if(Client.getInstance().getUser().isCloseTo(requesterId, epoch)) {
            Position position = Position
                    .newBuilder()
                    .setX(requesterXPos)
                    .setY(requesterYPos)
                    .build();

            LocationProof proof = LocationProof
                    .newBuilder()
                    .setProverId(requesterId)
                    .setWitnessId(Client.getInstance().getUser().getId())
                    .setEpoch(epoch)
                    .setPosition(position)
                    .build();

            SignedLocationProof response = SignedLocationProof.newBuilder().setLocationProof(proof).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            //TODO fazer mais bonito
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("You are not close to me bitch!").asRuntimeException());
        }
    }

}
