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


    public LocationReport.Builder requestLocationProofs(ArrayList<ClientServerGrpc.ClientServerStub> userStubs,
                                                        Client client, Long epoch, int f, ArrayList<Long> witnessesId)
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
        StreamObserver<SignedLocationProof> observer;
        for (int i = 0; i < userStubs.size(); i++) {
            final Long clientId = witnessesId.get(i);
            observer = new StreamObserver<>() {
                @Override
                public void onNext(SignedLocationProof response) {
                    LocationProof locationProof = response.getLocationProof();
                    try {
                        if(verifySignature(getUserPublicKey(locationProof.getWitnessId()), locationProof.toByteArray(),
                                response.getSignature().toByteArray()) && locationProof.getProverId() == client.getUser().getId()
                                && locationProof.getEpoch() == epoch && locationProof.getWitnessId() == clientId){

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
            userStubs.get(i).requestLocationProof(request, observer);
        }

        finishLatch.await();

        if(reportBuilder.getLocationProofCount() < f+1){
            throw new InvalidParameterException("Could not get enough clients to witness me considering the f.");
        }

        return reportBuilder;
    }

    public Boolean submitLocationReport(Client client, LocationReport.Builder reportBuilder)
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
        SubmitLocationReportRequest request;
        StreamObserver<SubmitLocationReportResponse> observer;
        for (int i = 0; i < serverStubs.size(); i++) {
            final int serverId = i + 1;
            observer = new StreamObserver<>() {
                @Override
                public void onNext(SubmitLocationReportResponse response) {
                    handleSubmitLocationReportResponse(response, key, serverId);
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

            byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(serverId));
            request = buildSubmitLocationReportRequest(encryptedKey, encryptedMessage, iv);
            serverStubs.get(i).submitLocationReport(request, observer);
        }

        finishLatch.await();
        if(ackList > (N_SERVERS + F)/2){
            resetAckList();
            return true;
        } else {
            System.err.println("No enough server responses for a quorum. This can only happen if " +
                    "there where more crashes or byzantine servers than supported, or the client crashed");
            return false;
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
        StreamObserver<ObtainLocationReportResponse> observer;
        for (int i = 0; i < serverStubs.size(); i++) {
            final int serverId = i + 1;
            observer = new StreamObserver<>() {
                @Override
                public void onNext(ObtainLocationReportResponse response) {
                    handleObtainLocationReportResponse(response, key, client, serverId, epoch);
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
            };

            byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(serverId));
            request = buildObtainLocationReportRequest(encryptedKey, encryptedMessage, iv);
            serverStubs.get(i).obtainLocationReport(request, observer);
        }

        finishLatch.await();

        LocationReport report = null;
        if(readList.size() > (N_SERVERS + F)/2){ //if we have enough just unblock the main thread
            report = (LocationReport) highestVal(readList);
            resetReadList();
        } else {
            System.err.println("No enough server responses for a quorum. This can only happen if " +
                    "there where more crashes or byzantine servers than supported, or the client crashed");
        }
        return report;
    }

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
        StreamObserver<RequestMyProofsResponse> observer;

        for (int i = 0; i < serverStubs.size(); i++) {
            final int serverId = i + 1;
            observer = new StreamObserver<>() {
                @Override
                public void onNext(RequestMyProofsResponse response) {
                    handleObtainMyProofsResponse(response, key, client, serverId, epochs);
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
            };

            byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(serverId));
            request = buildRequestMyProofsRequest(encryptedKey, encryptedMessage, iv);
            serverStubs.get(i).requestMyProofs(request, observer);
        }

        finishLatch.await();

        Proofs proofs = null;
        if(readList.size() > (N_SERVERS + F)/2){ //if we have enough just unblock the main thread
            proofs = (Proofs) highestVal(readList);
            resetReadList();
            return proofs;
        } else {
            System.err.println("No enough server responses for a quorum. This can only happen if " +
                    "there where more crashes or byzantine servers than supported, or the client crashed");
        }
        return proofs;
    }



    private void handleSubmitLocationReportResponse(SubmitLocationReportResponse response, SecretKey key, int serverId){
        try {
            byte[] encryptedSignedAck = response.getEncryptedSignedAck().toByteArray();
            byte[] decryptedSignedAck = symmetricDecrypt(encryptedSignedAck, key, new IvParameterSpec(response.getIv().toByteArray()));

            SignedAck signedAck = SignedAck.parseFrom(decryptedSignedAck);
            Ack ack = signedAck.getAck();

            byte[] message = ack.toByteArray();
            byte[] signature = signedAck.getSignature().toByteArray();
            if(ack.getServerId() == serverId &&
                    ack.getWts() == wts &&
                    verifySignature(getServerPublicKey(ack.getServerId() + 1), message, signature)){
                if(ackList <= (N_SERVERS + F)/2){
                    ackList++;
                }
            }
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException |
                NoSuchPaddingException | InvalidAlgorithmParameterException | CertificateException | IOException |
                SignatureException e) {

            System.err.println("Received an incorrect server response with message: " + e.getMessage());
        }
    }

    private void handleObtainLocationReportResponse(ObtainLocationReportResponse response, SecretKey key, Client client, int serverStubId, Long epoch){
        try {
            byte[] encryptedBody = response.getEncryptedServerSignedSignedLocationReportRid().toByteArray();
            byte[] decryptedMessage = symmetricDecrypt(encryptedBody, key, new IvParameterSpec(response.getIv().toByteArray()));
            ServerSignedSignedLocationReportRid serverSignedLocationReport = ServerSignedSignedLocationReportRid.parseFrom(decryptedMessage);
            SignedLocationReportRid signedLocationReportRid = serverSignedLocationReport.getSignedLocationReportRid();
            SignedLocationReport signedLocationReport = signedLocationReportRid.getSignedLocationReport();
            LocationReport report = signedLocationReport.getLocationReport();

            if(signedLocationReportRid.getServerId() != serverStubId ||
                    signedLocationReportRid.getRid() != rid ||
                    report.getLocationInformation().getEpoch() != epoch){
                return;
            }

            byte[] message = signedLocationReport.toByteArray();
            byte[] signature = serverSignedLocationReport.getServerSignature().toByteArray();
            if(!verifySignature(getServerPublicKey(signedLocationReportRid.getServerId() + 1), message, signature)){
                return;
            }

            message = report.toByteArray();
            signature = signedLocationReport.getUserSignature().toByteArray();
            if(!verifySignature(getUserPublicKey(client.getUser().getId()), message, signature)){
                return;
            }

            if(readList.size() <= (N_SERVERS + F)/2){
                readList.add(new ReadAck(report.getWts(), report));
            }
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException |
                NoSuchPaddingException | InvalidAlgorithmParameterException | CertificateException | IOException |
                SignatureException e) {
            System.err.println("Received an incorrect server response with message: " + e.getMessage());
        }
    }

    private void handleObtainMyProofsResponse(RequestMyProofsResponse response, SecretKey key, Client client, int serverStubId, List<Long> epochs){
        try {
            byte[] encryptedBody = response.getEncryptedServerSignedProofs().toByteArray();
            byte[] decryptedMessage = symmetricDecrypt(encryptedBody, key, new IvParameterSpec(response.getIv().toByteArray()));
            ServerSignedProofs serverSignedProofs = ServerSignedProofs.parseFrom(decryptedMessage);
            Proofs proofs = serverSignedProofs.getProofs();

            if(proofs.getServerId() != serverStubId || proofs.getRid() != rid){
                return;
            }

            byte[] message = proofs.toByteArray();
            byte[] signature = serverSignedProofs.getServerSignature().toByteArray();
            if(!verifySignature(getServerPublicKey(proofs.getServerId() + 1), message, signature)){
                return;
            }

            for (SignedLocationProof signedLocationProof : proofs.getLocationProofList()){
                message = signedLocationProof.getLocationProof().toByteArray();
                signature = signedLocationProof.getSignature().toByteArray();
                if(!epochs.contains(signedLocationProof.getLocationProof().getEpoch()) ||
                        !verifySignature(getUserPublicKey(client.getUser().getId()), message, signature)){
                    return;
                }
            }

            if(readList.size() <= (N_SERVERS + F)/2){
                //TODO no idea where the wts is (its implicit)
                //readList.add(new ReadAck(report.getWts(), proofs));
            }
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException |
                NoSuchPaddingException | InvalidAlgorithmParameterException | CertificateException | IOException |
                SignatureException e) {
            System.err.println("Received an incorrect server response with message: " + e.getMessage());
        }
    }



}
