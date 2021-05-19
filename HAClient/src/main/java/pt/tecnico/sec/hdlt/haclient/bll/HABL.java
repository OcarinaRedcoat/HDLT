package pt.tecnico.sec.hdlt.haclient.bll;

import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.entities.ListUsersAtLocation;
import pt.tecnico.sec.hdlt.entities.SignedLocationReports;
import pt.tecnico.sec.hdlt.haclient.ha.HA;
import pt.tecnico.sec.hdlt.communication.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static pt.tecnico.sec.hdlt.utils.CryptographicUtils.*;
import static pt.tecnico.sec.hdlt.utils.FileUtils.getServerPublicKey;
import static pt.tecnico.sec.hdlt.utils.FileUtils.getUserPublicKey;
import static pt.tecnico.sec.hdlt.utils.GeneralUtils.F;
import static pt.tecnico.sec.hdlt.utils.GeneralUtils.N_SERVERS;
import static pt.tecnico.sec.hdlt.utils.ProtoUtils.*;
import static pt.tecnico.sec.hdlt.utils.ProtoUtils.buildSignedLocationReportWrite;

public class HABL {

    private ArrayList<LocationServerGrpc.LocationServerStub> serverStubs;
    private CountDownLatch finishLatch;
    private HA ha;

    private int acks;
    private int rid;
    private SignedLocationReports signedLocationReports;
    private ListUsersAtLocation listOfUsersAtLocation;
    private List<Ack> ackList;
    private Boolean reading;


    public HABL(HA ha, ArrayList<LocationServerGrpc.LocationServerStub> serverStubs){
       this.ha = ha;
       this.serverStubs = serverStubs;
       this.rid = 0;
       this.signedLocationReports = new SignedLocationReports();
       this.listOfUsersAtLocation = new ListUsersAtLocation();
       this.ackList = new LinkedList<>();
       this.reading = false;
    }

    private void resetFinishLatch(){ this.finishLatch = new CountDownLatch(serverStubs.size());}






    //FIXME: client privkey....
    public LocationReport submitLocationReport(SignedLocationReport signedLocationReport) throws BadPaddingException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, IllegalBlockSizeException,
            NoSuchPaddingException, InvalidKeyException, SignatureException, InterruptedException, CertificateException,
            IOException {

        SignedLocationReportWrite signedLocationReportWrite =
                buildSignedLocationReportWrite(signedLocationReport, this.rid, true);

        byte[] signature = sign(signedLocationReportWrite.toByteArray(), ha.getPrivateKey());
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
            this.acks = 0;
            if(reading){
                reading = false;
            }
            return signedLocationReport.getLocationReport();
        }
        return null;
    }



    public LocationReport obtainLocationReport(int userId, Long epoch)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchPaddingException,
            BadPaddingException, IllegalBlockSizeException, IOException, InvalidKeySpecException, InvalidAlgorithmParameterException, CertificateException, InterruptedException {

        this.rid++;
        this.signedLocationReports = new SignedLocationReports();
        this.reading = true;

        LocationQuery locationQuery = buildLocationQuery(userId, epoch, rid, true);
        byte[] signature = sign(locationQuery.toByteArray(), ha.getPrivateKey());
        SignedLocationQuery signedLocationQuery = buildSignedLocationQuery(locationQuery, signature);

        SecretKey key = generateSecretKey();
        IvParameterSpec iv = generateIv();
        byte[] encryptedMessage = symmetricEncrypt(signedLocationQuery.toByteArray(), key, iv);

        resetFinishLatch();
        ObtainLocationReportRequest request;
        StreamObserver<ObtainLocationReportResponse> observer;
        for (int i=0; i < serverStubs.size(); i++){
            final int serverId = i + 1;
            observer = new StreamObserver<>() {
                @Override
                public void onNext(ObtainLocationReportResponse obtainLocationReportResponse) {
                    handleObtainLocationReportResponse(obtainLocationReportResponse, key, serverId, userId, epoch);
                }

                @Override
                public void onError(Throwable throwable) {
                    finishLatch.countDown();
                }

                @Override
                public void onCompleted() {
                    finishLatch.countDown();
                    if (signedLocationReports.numberOfAcks() > (N_SERVERS + F)/2){
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

        SignedLocationReport signedLocationReport = null;
        LocationReport report = null;
        if(signedLocationReports.numberOfAcks() > (N_SERVERS + F)/2){ //if we have enough just unblock the main thread
            signedLocationReport = signedLocationReports.getBestLocationReport();
            this.signedLocationReports = new SignedLocationReports();
            report = submitLocationReport(signedLocationReport);
        }

        return report;
    }

    /* Params: pos, ep .....
     * Specification: returns a list of users that were at position pos at epoch ep
     */
    public List<SignedLocationReport> obtainUsersAtLocation(long x, long y, long ep)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException,
            BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, IOException, InvalidKeySpecException, CertificateException, InterruptedException {

        this.rid++;
        this.listOfUsersAtLocation = new ListUsersAtLocation();
        this.reading = true;

        Position pos = buildPosition(x, y);

        UsersAtLocationQuery usersAtLocationQuery = buildUsersAtLocationQuery(pos, ep, rid);

        byte[] signature = sign(usersAtLocationQuery.toByteArray(), ha.getPrivateKey());

        SignedUsersAtLocationQuery signedUsersAtLocationQuery = buildSignedUsersAtLocationQuery(usersAtLocationQuery, signature);


        SecretKey key = generateSecretKey();
        IvParameterSpec iv = generateIv();
        byte[] encryptedMessage = symmetricEncrypt(signedUsersAtLocationQuery.toByteArray(), key, iv);




        resetFinishLatch();
        ObtainUsersAtLocationRequest request;
        StreamObserver<ObtainUsersAtLocationResponse> observer;
        for (int i=0; i < serverStubs.size(); i++){
            final int serverId = i + 1;
            observer = new StreamObserver<ObtainUsersAtLocationResponse>() {
                @Override
                public void onNext(ObtainUsersAtLocationResponse obtainUsersAtLocationResponse) {
                        handleObtainUsersAtLocation(obtainUsersAtLocationResponse, key, serverId, ep, x, y);
                }

                @Override
                public void onError(Throwable throwable) {
                    finishLatch.countDown();
                }


                @Override
                public void onCompleted() {
                    finishLatch.countDown();
                    if (listOfUsersAtLocation.numberOfAcks() > (N_SERVERS + F)/2 ){
                        do{
                            finishLatch.countDown();
                        } while (finishLatch.getCount() != 0);
                    }
                }

            };
            byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(1));

            request = buildObtainUsersAtLocationRequest( encryptedMessage, iv, encryptedKey);
            serverStubs.get(i).obtainUsersAtLocation(request, observer);
        }

        finishLatch.await();


        SignedLocationReportList locationReports = null;
        List<SignedLocationReport> listOfSignedLocationReports = null;
        if(listOfUsersAtLocation.numberOfAcks() > (N_SERVERS + F)/2 ){
            locationReports = listOfUsersAtLocation.getBestSignedLocationReportList();
            listOfSignedLocationReports = locationReports.getSignedLocationReportListList();
            listOfUsersAtLocation = new ListUsersAtLocation();
        }

        return listOfSignedLocationReports;
    }



    private void handleObtainLocationReportResponse(ObtainLocationReportResponse response, SecretKey key, int serverId, int userId,Long epoch){
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
            if(!verifySignature(getUserPublicKey(userId), message, signature)){
                return;
            }

            signedLocationReports.addLocationReport(signedLocationReport);
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException |
                NoSuchPaddingException | InvalidAlgorithmParameterException | CertificateException | IOException |
                SignatureException e) {
            System.err.println("Received an incorrect server response with message: " + e.getMessage());
        }
    }


    //TODO: verify
    private void handleObtainUsersAtLocation(ObtainUsersAtLocationResponse response, SecretKey key, int serverId, Long epoch, long x, long y){
        try {
            byte[] encryptedBody = response.getEncryptedSignedLocationReportList().toByteArray();
            byte[] decryptedMessage = symmetricDecrypt(encryptedBody, key, new IvParameterSpec(response.getIv().toByteArray()));
            ServerSignedSignedLocationReportList serverSignedSignedLocationReportList = ServerSignedSignedLocationReportList.parseFrom(decryptedMessage);
            SignedLocationReportList signedLocationReportList = serverSignedSignedLocationReportList.getSignedLocationReportList();
            List<SignedLocationReport> signedLocationReports = signedLocationReportList.getSignedLocationReportListList();

            byte[] serverSignedSignedLocationReportListMessage = serverSignedSignedLocationReportList.toByteArray();
            byte[] serverSignature = serverSignedSignedLocationReportList.getServerSignature().toByteArray();

            if (signedLocationReportList.getRid() != rid ||
                    !verifySignature(getServerPublicKey(serverId), serverSignedSignedLocationReportListMessage, serverSignature)){
                return;
            }

            List<LocationReport> reportList = new ArrayList<>();
            for (SignedLocationReport signedLocationReport: signedLocationReports){
                LocationReport report = signedLocationReport.getLocationReport();
                if(report.getLocationInformation().getEpoch() != epoch || report.getLocationInformation().getPosition().getX() != x ||
                    report.getLocationInformation().getPosition().getY() != y){
                    return;
                }

                byte[] message = signedLocationReport.toByteArray();
                byte[] signature = signedLocationReport.getUserSignature().toByteArray();
                int userId = signedLocationReport.getLocationReport().getLocationInformation().getUserId();
                if (!verifySignature(getUserPublicKey(userId), message, signature)){
                    return;
                }
                reportList.add(report);
            }



            listOfUsersAtLocation.addReceivedSignedLocationReport(signedLocationReportList);
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException |
                NoSuchPaddingException | InvalidAlgorithmParameterException | CertificateException | IOException |
                SignatureException e) {
            System.err.println("Received an incorrect server response with message: " + e.getMessage());
        }
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
        } catch (IllegalBlockSizeException | InvalidKeyException | BadPaddingException | NoSuchAlgorithmException |
                NoSuchPaddingException | InvalidAlgorithmParameterException | CertificateException | IOException |
                SignatureException e) {

            System.err.println("Received an incorrect server response with message: " + e.getMessage());
        }
    }


}
