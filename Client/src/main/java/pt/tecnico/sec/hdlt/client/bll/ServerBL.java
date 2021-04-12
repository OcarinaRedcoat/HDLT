package pt.tecnico.sec.hdlt.client.bll;

import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.LocationInformation;
import pt.tecnico.sec.hdlt.communication.LocationProof;
import pt.tecnico.sec.hdlt.communication.Position;
import pt.tecnico.sec.hdlt.communication.SignedLocationProof;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.sign;

public class ServerBL {

    public static void requestLocationProof(LocationInformation req, StreamObserver<SignedLocationProof> responseObserver)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
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

            byte[] signature = sign(proof.toByteArray(), Client.getInstance().getPrivKey());

            SignedLocationProof response = SignedLocationProof.newBuilder()
                    .setLocationProof(proof)
                    .setSignature(ByteString.copyFrom(signature))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("You are not close to me.").asRuntimeException());
        }
    }

}
