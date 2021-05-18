package pt.tecnico.sec.hdlt.client.services;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.client.bll.ResponseBL;
import pt.tecnico.sec.hdlt.entities.Client;
import pt.tecnico.sec.hdlt.communication.*;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;


class Impl extends ClientToClientGrpc.ClientToClientImplBase{

    private Client client;

    public Impl(Client client) {
        this.client = client;
    }

    @Override
    public void requestLocationProof(LocationInformationRequest req, StreamObserver<SignedLocationProof> responseObserver){
        try {
            SignedLocationProof response = ResponseBL.requestLocationProof(client, req, responseObserver);
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (NoSuchAlgorithmException | SignatureException | InvalidKeyException | InvalidKeySpecException |
                IOException | CertificateException e) {
            e.printStackTrace();
        } catch (InvalidParameterException e) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("You are not close to me.").asRuntimeException());
        }
    }

}
