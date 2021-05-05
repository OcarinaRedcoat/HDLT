package pt.tecnico.sec.hdlt.client.bll;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.Deadline;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.entities.Client;
import pt.tecnico.sec.hdlt.communication.*;
import pt.tecnico.sec.hdlt.entities.ReadAck;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static pt.tecnico.sec.hdlt.utils.DependableUtils.highestVal;
import static pt.tecnico.sec.hdlt.utils.FileUtils.getServerPublicKey;
import static pt.tecnico.sec.hdlt.utils.FileUtils.getUserPublicKey;
import static pt.tecnico.sec.hdlt.utils.CryptographicUtils.*;
import static pt.tecnico.sec.hdlt.utils.GeneralUtils.F;
import static pt.tecnico.sec.hdlt.utils.GeneralUtils.N_SERVERS;
import static pt.tecnico.sec.hdlt.utils.ProtoUtils.*;

public class ClientBL {

    private ArrayList<LocationServerGrpc.LocationServerStub> serverStubs;
    private CountDownLatch finishLatch;

    private int wts;
    private int ackList;
    private int rid;
    private List<ReadAck> readList;

    private int iAux;

    public ClientBL(ArrayList<LocationServerGrpc.LocationServerStub> serverStubs) {
        this.serverStubs = serverStubs;
        this.wts = 0;
        this.ackList = 0;
        this.rid = 0;
        this.readList = new LinkedList<>();
    }

    private void resetFinishLatch(){
        this.finishLatch = new CountDownLatch(serverStubs.size());
    }

    private void resetAckList(){
        this.ackList = 0;
    }

    private void resetReadList(){
        this.readList = new LinkedList<>();
    }


    public static LocationReport.Builder requestLocationProofs(ArrayList<ClientServerGrpc.ClientServerStub> userStubs, Client client, Long epoch, int f)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, InvalidKeySpecException,
            InvalidParameterException, CertificateException, InterruptedException {

        LocationInformation locationInformation = buildLocationInformation(client, epoch);
        byte[] signature = sign(locationInformation.toByteArray(), client.getPrivKey());
        LocationInformationRequest request = buildLocationInformationRequest(locationInformation, signature);

        LocationReport.Builder reportBuilder = LocationReport
                .newBuilder()
                .setNonce(generateNonce())
                .setLocationInformation(locationInformation);

        final CountDownLatch finishLatch = new CountDownLatch(userStubs.size());
        StreamObserver<SignedLocationProof> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(SignedLocationProof response) {
                LocationProof locationProof = response.getLocationProof();
                try {
                    if(verifySignature(getUserPublicKey(locationProof.getWitnessId()), locationProof.toByteArray(),
                            response.getSignature().toByteArray()) && locationProof.getProverId() == client.getUser().getId()
                            && locationProof.getEpoch() == epoch){

                        reportBuilder.addLocationProof(response);
                    }
                } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException |
                        CertificateException | IOException e) {
                    System.err.println("Something unexpected happen during signature verification.");
                }
            }

            @Override
            public void onError(Throwable t) {
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        for (ClientServerGrpc.ClientServerStub stub: userStubs){
            stub.requestLocationProof(request, responseObserver);
        }

        finishLatch.await();

        if(reportBuilder.getLocationProofCount() < f+1){
            throw new InvalidParameterException("Could not get enough clients to witness me considering the f.");
        }

        return reportBuilder;
    }

    public void submitLocationReport(Client client, LocationReport.Builder reportBuilder)
            throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, NoSuchPaddingException,
            IllegalBlockSizeException, InvalidAlgorithmParameterException, CertificateException, IOException,
            InterruptedException, SignatureException {

        this.wts++;
        LocationReport report = reportBuilder.setWts(this.wts).build();
        byte[] signature = sign(report.toByteArray(), client.getPrivKey());
        SignedLocationReport signedLocationReport = buildSignedLocationReport(report, signature);

        SecretKey key = generateSecretKey();
        IvParameterSpec iv = generateIv();
        byte[] encryptedMessage = symmetricEncrypt(signedLocationReport.toByteArray(), key, iv);

        resetFinishLatch();
        StreamObserver<SubmitLocationReportResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(SubmitLocationReportResponse response) {
                handleSubmitLocationReportResponse(response, key);
            }

            @Override
            public void onError(Throwable t) {
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                if(ackList > (N_SERVERS + F)/2){ //if we have enough just unblock the main thread
                    do{
                        finishLatch.countDown();
                    } while(finishLatch.getCount() != 1);
                }
                finishLatch.countDown();
            }
        };

        SubmitLocationReportRequest request;
        for (int i = 0; i < serverStubs.size(); i++) {
            byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(i+1));
            request = buildSubmitLocationReportRequest(encryptedKey, encryptedMessage, iv);
            serverStubs.get(i).submitLocationReport(request, responseObserver);
        }

        finishLatch.await();
        if(ackList > (N_SERVERS + F)/2){
            resetAckList();
        } else {
            //TODO Don't know what to do in this case (do we reset the ack list? if yes need to change restAck method)
            throw new InvalidParameterException("No quorum");
        }
    }

    public LocationReport obtainLocationReport(Client client, Long epoch)
            throws NoSuchAlgorithmException, SignatureException, InvalidKeyException, InvalidAlgorithmParameterException,
            BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, CertificateException, IOException, InterruptedException {

        rid++;
        LocationQuery locationQuery = buildLocationQuery(client, epoch, rid);
        byte[] signature = sign(locationQuery.toByteArray(), client.getPrivKey());
        SignedLocationQuery signedLocationQuery = buildSignedLocationQuery(locationQuery, signature);

        SecretKey key = generateSecretKey();
        IvParameterSpec iv = generateIv();
        byte[] encryptedMessage = symmetricEncrypt(signedLocationQuery.toByteArray(), key, iv);

        resetFinishLatch();
        ObtainLocationReportRequest request;
        List<StreamObserver<ObtainLocationReportResponse>> listObserver = new ArrayList<>();

        for (iAux = 0; iAux < serverStubs.size(); iAux++) {
            listObserver.add(new StreamObserver<>() {
                @Override
                public void onNext(ObtainLocationReportResponse response) {
                    int serverId = iAux;
                    handleObtainLocationReportResponse(response, key, client, serverId);
                }

                @Override
                public void onError(Throwable t) {
                    finishLatch.countDown();
                }

                @Override
                public void onCompleted() {
                    if(readList.size() > (N_SERVERS + F)/2){ //if we have enough just unblock the main thread
                        do{
                            finishLatch.countDown();
                        } while(finishLatch.getCount() != 1);
                    }
                    finishLatch.countDown();
                }
            });

            byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(iAux + 1));
            request = buildObtainLocationReportRequest(encryptedKey, encryptedMessage, iv);
            serverStubs.get(iAux).obtainLocationReport(request, listObserver.get(iAux));
        }

        finishLatch.await();

        if(readList.size() > (N_SERVERS + F)/2){ //if we have enough just unblock the main thread
            LocationReport report = (LocationReport) highestVal(readList);
            resetReadList();
            return report;
        } else {
            throw new InvalidParameterException("No enough responses for a quorum. This can only happen if there where more crashes or byzantine servers than supported, or the client crashes");
        }
    }

    //TODO falta amndar as epochs
    public Proofs ObtainMyProofs(Client client, List<Long> epochs)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchPaddingException,
            BadPaddingException, IllegalBlockSizeException, IOException, InvalidKeySpecException,
            InvalidAlgorithmParameterException, CertificateException, InterruptedException {

        rid++;
        ProofsQuery proofsQuery = buildProofsQuery(client, rid, epochs);
        byte[] signature = sign(proofsQuery.toByteArray(), client.getPrivKey());
        SignedProofsQuery signedProofsQuery = buildSignedProofsQuery(proofsQuery, signature);

        SecretKey key = generateSecretKey();
        IvParameterSpec iv = generateIv();
        byte[] encryptedMessage = symmetricEncrypt(signedProofsQuery.toByteArray(), key, iv);

        resetFinishLatch();
        RequestMyProofsRequest request;
        List<StreamObserver<RequestMyProofsResponse>> listObserver = new ArrayList<>();

        for (iAux = 0; iAux < serverStubs.size(); iAux++) {
            listObserver.add(new StreamObserver<>() {
                @Override
                public void onNext(RequestMyProofsResponse response) {
                    int serverId = iAux+1;
                    handleObtainMyProofsResponse(response, key, client, serverId);
                }

                @Override
                public void onError(Throwable t) {
                    finishLatch.countDown();
                }

                @Override
                public void onCompleted() {
                    if(readList.size() > (N_SERVERS + F)/2){ //if we have enough just unblock the main thread
                        do{
                            finishLatch.countDown();
                        } while(finishLatch.getCount() != 1);
                    }
                    finishLatch.countDown();
                }
            });

            byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(iAux + 1));
            request = buildRequestMyProofsRequest(encryptedKey, encryptedMessage, iv);
            serverStubs.get(iAux).requestMyProofs(request, listObserver.get(iAux));
        }

        finishLatch.await();

        if(readList.size() > (N_SERVERS + F)/2){ //if we have enough just unblock the main thread
            Proofs proofs = (Proofs) highestVal(readList);
            resetReadList();
            return proofs;
        } else {
            throw new InvalidParameterException("No enough responses for a quorum. This can only happen if there where more crashes or byzantine servers than supported, or the client crashes");
        }
    }


    private void handleSubmitLocationReportResponse(SubmitLocationReportResponse response, SecretKey key){
        try {
            byte[] encryptedSignedAck = response.getEncryptedSignedAck().toByteArray();
            byte[] decryptedSignedAck = symmetricDecrypt(encryptedSignedAck, key, new IvParameterSpec(response.getIv().toByteArray()));

            SignedAck signedAck = SignedAck.parseFrom(decryptedSignedAck);
            Ack ack = signedAck.getAck();

            byte[] message = ack.toByteArray();
            byte[] signature = signedAck.getSignature().toByteArray();
            if(verifySignature(getServerPublicKey(ack.getServerId() + 1), message, signature)){
                if(ack.getWts() == wts){
                    if(ackList <= (N_SERVERS + F)/2){
                        ackList++;
                    }
                }
            }
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException |
                NoSuchPaddingException | InvalidAlgorithmParameterException | CertificateException | IOException |
                SignatureException e) {

            //TODO do something?
        }
    }

    private void handleObtainLocationReportResponse(ObtainLocationReportResponse response, SecretKey key, Client client, int serverStubId){
        try {
            byte[] encryptedBody = response.getEncryptedServerSignedSignedLocationReportRid().toByteArray();
            byte[] decryptedMessage = symmetricDecrypt(encryptedBody, key, new IvParameterSpec(response.getIv().toByteArray()));
            ServerSignedSignedLocationReportRid serverSignedLocationReport = ServerSignedSignedLocationReportRid.parseFrom(decryptedMessage);
            SignedLocationReportRid signedLocationReportRid = serverSignedLocationReport.getSignedLocationReportRid();
            SignedLocationReport signedLocationReport = signedLocationReportRid.getSignedLocationReport();
            LocationReport report = signedLocationReport.getLocationReport();

            if(signedLocationReportRid.getServerId() != serverStubId){
                return;
            }

            if(signedLocationReportRid.getRid() != rid){
                return;
            }

            //Ignore cases where server signature does not match
            byte[] message = signedLocationReport.toByteArray();
            byte[] signature = serverSignedLocationReport.getServerSignature().toByteArray();
            if(!verifySignature(getServerPublicKey(signedLocationReportRid.getServerId() + 1), message, signature)){
                return;
            }

            //Ignore cases where my signature does not match
            message = report.toByteArray();
            signature = signedLocationReport.getUserSignature().toByteArray();
            if(!verifySignature(getUserPublicKey(client.getUser().getId()), message, signature)){
                return;
            }

            //TODO verify report epoch is the one we asked

            if(readList.size() > (N_SERVERS + F)/2){
                readList.add(new ReadAck(report.getWts(), report));
            }
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException |
                NoSuchPaddingException | InvalidAlgorithmParameterException | CertificateException | IOException |
                SignatureException e) {

            //TODO do something?
        }
    }

    private void handleObtainMyProofsResponse(RequestMyProofsResponse response, SecretKey key, Client client, int serverStubId){
        try {
            byte[] encryptedBody = response.getEncryptedServerSignedProofs().toByteArray();
            byte[] decryptedMessage = symmetricDecrypt(encryptedBody, key, new IvParameterSpec(response.getIv().toByteArray()));
            ServerSignedProofs serverSignedProofs = ServerSignedProofs.parseFrom(decryptedMessage);
            Proofs proofs = serverSignedProofs.getProofs();

            if(proofs.getServerId() != serverStubId){
                return;
            }

            if(proofs.getRid() != rid){
                return;
            }

            //Ignore cases where server signature does not match
            byte[] message = proofs.toByteArray();
            byte[] signature = serverSignedProofs.getServerSignature().toByteArray();
            if(!verifySignature(getServerPublicKey(proofs.getServerId() + 1), message, signature)){
                return;
            }

            //Ignore cases where my signature does not match the proof TODO this is needed to verify the wts but i don't know how it is done here
            for (SignedLocationProof signedLocationProof : proofs.getLocationProofList()){
                message = signedLocationProof.getLocationProof().toByteArray();
                signature = signedLocationProof.getSignature().toByteArray();
                if(!verifySignature(getUserPublicKey(client.getUser().getId()), message, signature)){
                    return;
                }
            }

            //TODO verify proofs are from the epochs we asked

            if(readList.size() > (N_SERVERS + F)/2){
                //TODO no idea where the wts is (its implicit)
                //readList.add(new ReadAck(report.getWts(), proofs));
            }
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException |
                NoSuchPaddingException | InvalidAlgorithmParameterException | CertificateException | IOException |
                SignatureException e) {

            //TODO do something?
        }
    }



}
