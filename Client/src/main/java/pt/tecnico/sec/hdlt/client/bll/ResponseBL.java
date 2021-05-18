package pt.tecnico.sec.hdlt.client.bll;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.entities.User;
import pt.tecnico.sec.hdlt.entities.Client;
import pt.tecnico.sec.hdlt.communication.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import static pt.tecnico.sec.hdlt.utils.FileUtils.getUserPublicKey;
import static pt.tecnico.sec.hdlt.utils.CryptographicUtils.sign;
import static pt.tecnico.sec.hdlt.utils.CryptographicUtils.verifySignature;
import static pt.tecnico.sec.hdlt.utils.ProtoUtils.*;

public class ResponseBL {

    public static SignedLocationProof requestLocationProof(Client client, LocationInformationRequest req, StreamObserver<SignedLocationProof> responseObserver)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidParameterException, IOException,
            InvalidKeySpecException, CertificateException {

        LocationInformation locationInformation = req.getLocationInformation();
        int requesterId = locationInformation.getUserId();
        long epoch = locationInformation.getEpoch();

        if(!verifySignature(getUserPublicKey(requesterId), locationInformation.toByteArray(),
                req.getSignature().toByteArray())){

            throw new InvalidParameterException("Invalid location report client signature");
        }

        if(client.getUser().isCloseTo(requesterId, epoch)) {
            LocationProof proof = buildLocationProof(client, epoch, requesterId);

            byte[] signature = sign(proof.toByteArray(), client.getPrivKey());

            return buildSignedLocationProof(proof, signature);
        } else {
            throw new InvalidParameterException("Invalid requester Id, i am not close to him.");
        }
    }

}
