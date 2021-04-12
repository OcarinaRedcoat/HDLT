package pt.tecnico.sec.hdlt.client.communication;

import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.client.bll.ServerBL;
import pt.tecnico.sec.hdlt.communication.*;

import java.security.InvalidKeyException;
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
            ServerBL.requestLocationProof(req, responseObserver);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
        }
    }

}
