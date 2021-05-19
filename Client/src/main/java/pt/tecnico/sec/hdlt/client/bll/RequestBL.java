package pt.tecnico.sec.hdlt.client.bll;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.entities.Client;
import pt.tecnico.sec.hdlt.communication.*;
import pt.tecnico.sec.hdlt.entities.ListOfReceivedMyProofs;
import pt.tecnico.sec.hdlt.entities.SignedLocationReports;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static pt.tecnico.sec.hdlt.utils.FileUtils.getServerPublicKey;
import static pt.tecnico.sec.hdlt.utils.FileUtils.getUserPublicKey;
import static pt.tecnico.sec.hdlt.utils.CryptographicUtils.*;
import static pt.tecnico.sec.hdlt.utils.GeneralUtils.F;
import static pt.tecnico.sec.hdlt.utils.GeneralUtils.N_SERVERS;
import static pt.tecnico.sec.hdlt.utils.ProtoUtils.*;
import static pt.tecnico.sec.hdlt.utils.ProtoUtils.buildSignedLocationReportWrite;

public class RequestBL {

    private final ArrayList<LocationServerGrpc.LocationServerStub> serverStubs;
    private CountDownLatch finishLatch;
    private final Client client;

    private int rid;
    private ListOfReceivedMyProofs listOfReceivedMyProofs;
    private SignedLocationReports signedLocationReports;
    private final List<Ack> ackList;
    private Boolean reading;

    public RequestBL(Client client, ArrayList<LocationServerGrpc.LocationServerStub> serverStubs) {
        this.client = client;
        this.serverStubs = serverStubs;
        this.rid = 0;
        this.listOfReceivedMyProofs = new ListOfReceivedMyProofs();
        this.signedLocationReports = new SignedLocationReports();
        this.ackList = new LinkedList<>();
        this.reading = false;
    }

    private void resetFinishLatch(){
        this.finishLatch = new CountDownLatch(serverStubs.size());
    }


    public LocationReport.Builder requestLocationProofs(ArrayList<ClientToClientGrpc.ClientToClientStub> userStubs,
                                                        Long epoch, int f, ArrayList<Long> witnessesId)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InterruptedException {

        LocationInformation locationInformation = buildLocationInformation(client, epoch);
        byte[] signature = sign(locationInformation.toByteArray(), client.getPrivKey());
        LocationInformationRequest request = buildLocationInformationRequest(locationInformation, signature);

        LocationReport.Builder reportBuilder = LocationReport
                .newBuilder()
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
                    } catch (IOException | CertificateException | NoSuchAlgorithmException | InvalidKeyException |
                            SignatureException e) {
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

    public LocationReport submitLocationReport(LocationReport.Builder reportBuilder) throws BadPaddingException,
            InterruptedException, InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException,
            SignatureException, NoSuchPaddingException, InvalidAlgorithmParameterException, CertificateException,
            IOException {

        this.rid++;
        LocationReport report = reportBuilder
                .build();
        return submitLocationReport(report);
    }

    public LocationReport submitLocationReport(LocationReport report) throws BadPaddingException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException,
            NoSuchPaddingException, InvalidKeyException, SignatureException, InterruptedException, CertificateException,
            IOException {

        byte[] signature = sign(report.toByteArray(), client.getPrivKey());
        SignedLocationReport signedLocationReport = buildSignedLocationReport(report, signature);

        SignedLocationReportWrite signedLocationReportWrite =
                buildSignedLocationReportWrite(signedLocationReport, this.rid, false);

        signature = sign(signedLocationReportWrite.toByteArray(), client.getPrivKey());
        AuthenticatedSignedLocationReportWrite authenticatedSignedLocationReportWrite =
                buildAuthenticatedSignedLocationReportWrite(signedLocationReportWrite, signature);

        SecretKey key = generateSecretKey();
        IvParameterSpec iv = generateIv();
        byte[] encryptedMessage = symmetricEncrypt(authenticatedSignedLocationReportWrite.toByteArray(), key, iv);

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
                    finishLatch.countDown();
                    if(ackList.size() > (N_SERVERS + F)/2){ //if we have enough just unblock the main thread
                        do{
                            finishLatch.countDown();
                        } while(finishLatch.getCount() != 0);
                    }
                }
            };

            byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(serverId));
            request = buildSubmitLocationReportRequest(encryptedKey, encryptedMessage, iv);
            serverStubs.get(i).submitLocationReport(request, observer);
        }

        finishLatch.await();
        if(ackList.size() > (N_SERVERS + F)/2){
            if(reading){
                reading = false;
            }
            return report;
        }
        return null;
    }

    public LocationReport obtainLocationReport(Long epoch) throws NoSuchAlgorithmException, InvalidKeyException,
            BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException,
            SignatureException, InterruptedException, CertificateException, IOException {

        this.rid++;
        this.signedLocationReports = new SignedLocationReports();
        this.reading = true;

        LocationQuery locationQuery = buildLocationQuery(client.getUser().getId(), epoch, rid, false);
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
                    handleObtainLocationReportResponse(response, key, serverId, epoch);
                }

                @Override
                public void onError(Throwable t) {
                    finishLatch.countDown();
                }

                @Override
                public void onCompleted() {
                    finishLatch.countDown();
                    if(signedLocationReports.numberOfAcks() > (N_SERVERS + F)/2){ //if we have enough just unblock the main thread
                        do{
                            finishLatch.countDown();
                        } while(finishLatch.getCount() != 0);
                    }
                }
            };

            byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(serverId));
            request = buildObtainLocationReportRequest(encryptedKey, encryptedMessage, iv);
            serverStubs.get(i).obtainLocationReport(request, observer);
        }

        finishLatch.await();

        LocationReport report = null;
        if(signedLocationReports.numberOfAcks() > (N_SERVERS + F)/2){ //if we have enough just unblock the main thread
            report = this.signedLocationReports.getBestLocationReport().getLocationReport();
            this.signedLocationReports = new SignedLocationReports();
            report = submitLocationReport(report);
        }
        return report;
    }

    public Proofs ObtainMyProofs(List<Long> epochs) throws NoSuchAlgorithmException, InvalidKeyException,
            SignatureException, InvalidAlgorithmParameterException, BadPaddingException, NoSuchPaddingException,
            IllegalBlockSizeException, InterruptedException, CertificateException, IOException {

        this.rid++;
        this.listOfReceivedMyProofs = new ListOfReceivedMyProofs();

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
                    handleRequestMyProofsResponse(response, key, serverId, epochs);
                }

                @Override
                public void onError(Throwable t) {
                    finishLatch.countDown();
                }

                @Override
                public void onCompleted() {
                    finishLatch.countDown();
                    if(listOfReceivedMyProofs.numberOfAcks() > (N_SERVERS + F)/2){ //if we have enough just unblock the main thread
                        do{
                            finishLatch.countDown();
                        } while(finishLatch.getCount() != 0);
                    }
                }
            };

            byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(serverId));
            request = buildRequestMyProofsRequest(encryptedKey, encryptedMessage, iv);
            serverStubs.get(i).requestMyProofs(request, observer);
        }

        finishLatch.await();

        Proofs proofs = null;
        if(listOfReceivedMyProofs.numberOfAcks() > (N_SERVERS + F)/2){ //if we have enough just unblock the main thread
            proofs = listOfReceivedMyProofs.getBestProofs();
            listOfReceivedMyProofs = new ListOfReceivedMyProofs();
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
            if(verifySignature(getServerPublicKey(serverId), message, signature)){
                ackList.add(ack);
            }
        } catch (Exception e) {
            System.err.println("Received an incorrect server response with message: " + e.getMessage());
        }
    }

    private void handleObtainLocationReportResponse(ObtainLocationReportResponse response, SecretKey key, int serverId, Long epoch){
        try {
            byte[] encryptedBody = response.getEncryptedServerSignedSignedLocationReportRid().toByteArray();
            byte[] decryptedMessage = symmetricDecrypt(encryptedBody, key, new IvParameterSpec(response.getIv().toByteArray()));
            ServerSignedSignedLocationReportRid serverSignedLocationReport = ServerSignedSignedLocationReportRid.parseFrom(decryptedMessage);
            SignedLocationReportRid signedLocationReportRid = serverSignedLocationReport.getSignedLocationReportRid();
            SignedLocationReport signedLocationReport = signedLocationReportRid.getSignedLocationReport();
            LocationReport report = signedLocationReport.getLocationReport();

            if(signedLocationReportRid.getRid() != rid || report.getLocationInformation().getEpoch() != epoch){
                return;
            }

            byte[] message = signedLocationReportRid.toByteArray();
            byte[] signature = serverSignedLocationReport.getServerSignature().toByteArray();
            if(!verifySignature(getServerPublicKey(serverId), message, signature)){
                return;
            }

            message = report.toByteArray();
            signature = signedLocationReport.getUserSignature().toByteArray();
            if(!verifySignature(getUserPublicKey(client.getUser().getId()), message, signature)){
                return;
            }

            signedLocationReports.addLocationReport(signedLocationReport);
        } catch (Exception e) {
            System.err.println("Received an incorrect server response with message: " + e.getMessage());
        }
    }

    private void handleRequestMyProofsResponse(RequestMyProofsResponse response, SecretKey key, int serverId, List<Long> epochs){
        try {
            byte[] encryptedBody = response.getEncryptedServerSignedProofs().toByteArray();
            byte[] decryptedMessage = symmetricDecrypt(encryptedBody, key, new IvParameterSpec(response.getIv().toByteArray()));
            ServerSignedProofs serverSignedProofs = ServerSignedProofs.parseFrom(decryptedMessage);
            Proofs proofs = serverSignedProofs.getProofs();

            if(proofs.getRid() != rid){
                return;
            }

            byte[] message = proofs.toByteArray();
            byte[] signature = serverSignedProofs.getServerSignature().toByteArray();
            if(!verifySignature(getServerPublicKey(serverId), message, signature)){
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

            listOfReceivedMyProofs.addReceivedProofs(proofs);
        } catch (Exception e) {
            System.err.println("Received an incorrect server response with message: " + e.getMessage());
        }
    }



}
