package pt.tecnico.sec.hdlt.client.user;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.client.Grid;
import pt.tecnico.sec.hdlt.communication.LocationProof;
import pt.tecnico.sec.hdlt.communication.LocationProofRequest;
import pt.tecnico.sec.hdlt.communication.LocationProofResponse;
import pt.tecnico.sec.hdlt.communication.LocationServerGrpc;

class UserImpl extends LocationServerGrpc.LocationServerImplBase{

    User myUser;
    Grid currentGrid;

    public UserImpl(User myUser){
        this.myUser = myUser;
    }

    public void setGird(Grid cGrid){
        this.currentGrid = cGrid;
    }

    //TODO: FAZER AS EXCEPCOES!!!!!
    @Override
    public void requestLocationProof(LocationProofRequest req, StreamObserver<LocationProofResponse> responseObserver){
        int requesterId = Integer.parseInt(req.getUserId());
        int currentEpoch = req.getEpoch();
        int requesterX = req.getRequesterX();
        int requesterY = req.getRequesterY();

        for (Long reqId: myUser.getCloseBy()) {
            if (reqId == requesterId){
                User usr = this.currentGrid.getMyUser(requesterId, currentEpoch);
                if(usr.getX_position() == requesterX && usr.getY_position() == requesterY){

                    LocationProof proof = LocationProof.newBuilder()
                            .setRequesterX(requesterX).setRequesterY(requesterY).setRequestedX(usr.getX_position())
                            .setRequesterY(usr.getY_position()).setRequesterUserId(String.valueOf(requesterId))
                            .setRequestedUserId(String.valueOf(usr.getId())).setEpoch(currentEpoch).build();

                    LocationProofResponse response = LocationProofResponse.newBuilder().setProof(proof).build();

                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                }
            }
        }

    }
}
