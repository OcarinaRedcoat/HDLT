package pt.tecnico.sec.hdlt.client.communication;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.client.bll.ServerBL;
import pt.tecnico.sec.hdlt.entities.Client;
import pt.tecnico.sec.hdlt.communication.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;


class UserImpl extends ClientServerGrpc.ClientServerImplBase{

    private static final Logger logger = Logger.getLogger(UserClient.class.getName());

    private Client client;

    public UserImpl(Client client) {
        this.client = client;
    }

    @Override
    public void requestLocationProof(LocationInformationRequest req, StreamObserver<SignedLocationProof> responseObserver){
        logger.info("Someone asked for me to witness them");

        try {
            SignedLocationProof response = ServerBL.requestLocationProof(client, req, responseObserver);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | InvalidKeySpecException |
                IOException | CertificateException e) {
            e.printStackTrace();
        } catch (InvalidParameterException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("You are not close to me.").asRuntimeException());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

}
