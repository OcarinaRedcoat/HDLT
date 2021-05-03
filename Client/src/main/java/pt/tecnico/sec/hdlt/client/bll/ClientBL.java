package pt.tecnico.sec.hdlt.client.bll;

import com.google.protobuf.ByteString;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.User;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.*;

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
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import static pt.tecnico.sec.hdlt.FileUtils.getServerPublicKey;
import static pt.tecnico.sec.hdlt.FileUtils.getUserPublicKey;
import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.*;
import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.symmetricDecrypt;

public class ClientBL {

    public static LocationReport requestLocationProofs(Client client, Long epoch, int f, ArrayList<ClientServerGrpc.ClientServerStub> userStubs)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, InvalidKeySpecException,
            InvalidParameterException, CertificateException, InterruptedException {


        Position position = Position
                .newBuilder()
                .setX(client.getUser().getPositionWithEpoch(epoch).getxPos())
                .setY(client.getUser().getPositionWithEpoch(epoch).getyPos())
                .build();

        LocationInformation locationInformation = LocationInformation
                .newBuilder()
                .setUserId(client.getUser().getId())
                .setEpoch(epoch)
                .setPosition(position)
                .build();

        byte[] signature = sign(locationInformation.toByteArray(), client.getPrivKey());

        LocationInformationRequest request = LocationInformationRequest
                .newBuilder()
                .setLocationInformation(locationInformation)
                .setSignature(ByteString.copyFrom(signature))
                .build();

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
        return reportBuilder.build();
    }


    public static void submitLocationReport(Client client, LocationReport report, ArrayList<LocationServerGrpc.LocationServerStub> serverStubs)
            throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, NoSuchPaddingException,
            IllegalBlockSizeException, IOException, InvalidKeySpecException, InvalidAlgorithmParameterException,
            SignatureException, CertificateException, InterruptedException {

        byte[] signature = sign(report.toByteArray(), client.getPrivKey());

        SignedLocationReport signedLocationReport = SignedLocationReport
                .newBuilder()
                .setLocationReport(report)
                .setUserSignature(ByteString.copyFrom(signature))
                .build();

        SecretKey key = generateSecretKey();
        IvParameterSpec iv = generateIv();
        byte[] encryptedMessage = symmetricEncrypt(signedLocationReport.toByteArray(), key, iv);
        byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(1));

        SubmitLocationReportRequest request = SubmitLocationReportRequest
                .newBuilder()
                .setKey(ByteString.copyFrom(encryptedKey))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .setEncryptedSignedLocationReport(ByteString.copyFrom(encryptedMessage))
                .build();

        final CountDownLatch finishLatch = new CountDownLatch(serverStubs.size());
        StreamObserver<SubmitLocationReportResponse> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(SubmitLocationReportResponse response) {

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

        for (LocationServerGrpc.LocationServerStub stub: serverStubs){
            stub.submitLocationReport(request, responseObserver);
        }

        finishLatch.await();
    }

    public static LocationReport obtainLocationReport(Client client, Long epoch, ArrayList<LocationServerGrpc.LocationServerStub> serverStubs)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchPaddingException,
            BadPaddingException, IllegalBlockSizeException, IOException, InvalidKeySpecException,
            InvalidAlgorithmParameterException, CertificateException {

        //TODO Not implemented
        return LocationReport.newBuilder().build();
//        LocationQuery locationQuery = LocationQuery
//                .newBuilder()
//                .setUserId(client.getUser().getId())
//                .setEpoch(epoch)
//                .setNonce(generateNonce())
//                .build();
//
//        byte[] signature = sign(locationQuery.toByteArray(), client.getPrivKey());
//
//        SignedLocationQuery signedLocationQuery = SignedLocationQuery
//                .newBuilder()
//                .setLocationQuery(locationQuery)
//                .setSignature(ByteString.copyFrom(signature))
//                .build();
//
//        SecretKey key = generateSecretKey();
//
//        IvParameterSpec iv = generateIv();
//        byte[] encryptedMessage = symmetricEncrypt(signedLocationQuery.toByteArray(), key, iv);
//        byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(1));
//
//        ObtainLocationReportRequest request = ObtainLocationReportRequest
//                .newBuilder()
//                .setKey(ByteString.copyFrom(encryptedKey))
//                .setIv(ByteString.copyFrom(iv.getIV()))
//                .setEncryptedSignedLocationQuery(ByteString.copyFrom(encryptedMessage))
//                .build();
//
//
//        ObtainLocationReportResponse response = serverStub.obtainLocationReport(request);
//
//        byte[] decryptedMessage = symmetricDecrypt(response.getEncryptedServerSignedSignedLocationReport().toByteArray(), key,
//                new IvParameterSpec(response.getIv().toByteArray()));
//
//        ServerSignedSignedLocationReport serverSignedSignedLocationReport = ServerSignedSignedLocationReport.parseFrom(decryptedMessage);
//
//        //Verify server signature
//        if(!verifySignature(getServerPublicKey(1), serverSignedSignedLocationReport.getSignedLocationReport().toByteArray(),
//                serverSignedSignedLocationReport.getServerSignature().toByteArray())){
//
//            throw new InvalidParameterException("Invalid response, server signature is wrong!");
//        }
//        SignedLocationReport signedLocationReport = serverSignedSignedLocationReport.getSignedLocationReport();
//
//        LocationReport report = signedLocationReport.getLocationReport();
//
//        //Verify my signature
//        if(!verifySignature(getUserPublicKey(client.getUser().getId()), report.toByteArray(),
//                signedLocationReport.getUserSignature().toByteArray())){
//
//            throw new InvalidParameterException("Invalid location report signature, it is not mine!");
//        }
//
//
//        return report;
    }

}
