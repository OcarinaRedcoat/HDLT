package pt.tecnico.sec.hdlt.client.bll;

import com.google.protobuf.ByteString;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import static pt.tecnico.sec.hdlt.FileUtils.getServerPublicKey;
import static pt.tecnico.sec.hdlt.FileUtils.getUserPublicKey;
import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.sign;
import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.verifySignature;

public class ServerBL {

    public static SignedLocationProof requestLocationProof(LocationInformationRequest req, StreamObserver<SignedLocationProof> responseObserver)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidParameterException, IOException,
            InvalidKeySpecException {

        LocationInformation locationInformation = req.getLocationInformation();
        int requesterId = locationInformation.getUserId();

        if(!verifySignature(getUserPublicKey(requesterId), locationInformation.toByteArray(),
                req.getSignature().toByteArray())){

            throw new InvalidParameterException("Invalid location report client signature");
        }

        long epoch = locationInformation.getEpoch();
        long requesterXPos = locationInformation.getPosition().getX();
        long requesterYPos = locationInformation.getPosition().getY();

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

            return SignedLocationProof.newBuilder()
                    .setLocationProof(proof)
                    .setSignature(ByteString.copyFrom(signature))
                    .build();
        } else {
            throw new InvalidParameterException("Invalid requester Id, i am not close to him.");
        }
    }

}
