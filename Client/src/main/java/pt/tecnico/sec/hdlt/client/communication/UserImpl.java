package pt.tecnico.sec.hdlt.client.communication;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.client.bll.ServerBL;
import pt.tecnico.sec.hdlt.communication.*;

import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.logging.Level;
import java.util.logging.Logger;


class UserImpl extends ClientServerGrpc.ClientServerImplBase{

    private static final Logger logger = Logger.getLogger(UserClient.class.getName());

    @Override
    public void requestLocationProof(LocationInformation req, StreamObserver<SignedLocationProof> responseObserver){
        logger.info("Someone asked for me to witness them");

        try {
            SignedLocationProof response = ServerBL.requestLocationProof(req, responseObserver);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (InvalidParameterException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("You are not close to me.").asRuntimeException());
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

}
