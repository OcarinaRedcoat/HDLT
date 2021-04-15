package pt.tecnico.sec.hdlt.client.bll;

import com.google.protobuf.ByteString;
import io.grpc.StatusRuntimeException;
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
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.logging.Level;

import static pt.tecnico.sec.hdlt.FileUtils.getServerPublicKey;
import static pt.tecnico.sec.hdlt.FileUtils.getUserPublicKey;
import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.*;
import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.symmetricDecrypt;

public class ClientBL {

    public static LocationReport requestLocationProofs(Client client, Long epoch, int f, ArrayList<ClientServerGrpc.ClientServerBlockingStub> userStubs)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException, InvalidKeySpecException,
            InvalidParameterException {

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
                .setLocationInformation(locationInformation);

        for (ClientServerGrpc.ClientServerBlockingStub stub: userStubs){
            if(reportBuilder.getLocationProofCount() == f+1)
                break;

            try {
                SignedLocationProof response = stub.requestLocationProof(request);

                LocationProof locationProof = response.getLocationProof();
                if(verifySignature(getUserPublicKey(locationProof.getWitnessId()), locationProof.toByteArray(),
                        response.getSignature().toByteArray()) && locationProof.getProverId() == client.getUser().getId()
                        && locationProof.getEpoch() == epoch){

                    reportBuilder.addLocationProof(response);
                } else {
                    System.err.println("Received a proof with an invalid signature!");
                }
            } catch (StatusRuntimeException e){
                System.err.println("Someone did not witness!");
            }
        }

        if(reportBuilder.getLocationProofCount() < f+1){
            throw new InvalidParameterException("Could not get enough clients to witness me considering the f.");
        }

        return reportBuilder.build();
    }

    public static void submitLocationReport(Client client, LocationReport report, LocationServerGrpc.LocationServerBlockingStub serverStub)
            throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, NoSuchPaddingException,
            IllegalBlockSizeException, IOException, InvalidKeySpecException, InvalidAlgorithmParameterException,
            SignatureException {

        byte[] signature = sign(report.toByteArray(), client.getPrivKey());

        SignedLocationReport signedLocationReport = SignedLocationReport
                .newBuilder()
                .setLocationReport(report)
                .setSignedLocationReport(ByteString.copyFrom(signature))
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

        serverStub.submitLocationReport(request);
    }

    public static LocationReport obtainLocationReport(Client client, Long epoch, LocationServerGrpc.LocationServerBlockingStub serverStub)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchPaddingException,
            BadPaddingException, IllegalBlockSizeException, IOException, InvalidKeySpecException,
            InvalidAlgorithmParameterException {

        LocationQuery locationQuery = LocationQuery
                .newBuilder()
                .setUserId(client.getUser().getId())
                .setEpoch(epoch)
                .build();

        byte[] signature = sign(locationQuery.toByteArray(), client.getPrivKey());

        SignedLocationQuery signedLocationQuery = SignedLocationQuery
                .newBuilder()
                .setLocationQuery(locationQuery)
                .setSignature(ByteString.copyFrom(signature))
                .build();

        SecretKey key = generateSecretKey();

        IvParameterSpec iv = generateIv();
        byte[] encryptedMessage = symmetricEncrypt(signedLocationQuery.toByteArray(), key, iv);
        byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(1));

        ObtainLocationReportRequest request = ObtainLocationReportRequest
                .newBuilder()
                .setKey(ByteString.copyFrom(encryptedKey))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .setEncryptedSignedLocationQuery(ByteString.copyFrom(encryptedMessage))
                .build();

        ObtainLocationReportResponse response = serverStub.obtainLocationReport(request);

        byte[] decryptedMessage = symmetricDecrypt(response.getEncryptedSignedLocationReport().toByteArray(), key,
                new IvParameterSpec(response.getIv().toByteArray()));

        SignedLocationReport signedLocationReport = SignedLocationReport.parseFrom(decryptedMessage);

        LocationReport report = signedLocationReport.getLocationReport();

        if(!verifySignature(getServerPublicKey(1), report.toByteArray(),
                signedLocationReport.getSignedLocationReport().toByteArray())){

            throw new InvalidParameterException("Invalid location report server signature");
        }

        return report;
    }

}
