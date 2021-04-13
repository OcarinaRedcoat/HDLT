package pt.tecnico.sec.hdlt.client.bll;

import com.google.protobuf.ByteString;
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
import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.*;
import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.symmetricDecrypt;

public class ClientBL {

    public static LocationReport requestLocationProofs(Long epoch, ArrayList<ClientServerGrpc.ClientServerBlockingStub> userStubs)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        User user = Client.getInstance().getUser();

        Position position = Position
                .newBuilder()
                .setX(user.getPositionWithEpoch(epoch).getxPos())
                .setY(user.getPositionWithEpoch(epoch).getyPos())
                .build();

        LocationInformation request = LocationInformation
                .newBuilder()
                .setUserId(user.getId())
                .setEpoch(epoch)
                .setPosition(position)
                .build();

        LocationReport.Builder reportBuilder = LocationReport
                .newBuilder()
                .setLocationInformation(request);

        for (ClientServerGrpc.ClientServerBlockingStub stub : userStubs) {
            try {
                SignedLocationProof response = stub.requestLocationProof(request);
                reportBuilder.addLocationProof(response);
            } catch (Exception e){
                System.err.println("Someone did not witness!");
            }
        }

        return reportBuilder.build();
    }

    public static void submitLocationReport(LocationReport report, LocationServerGrpc.LocationServerBlockingStub serverStub)
            throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, NoSuchPaddingException,
            IllegalBlockSizeException, IOException, InvalidKeySpecException, InvalidAlgorithmParameterException,
            SignatureException {

        byte[] signature = sign(report.toByteArray(), Client.getInstance().getPrivKey());

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

        //TODO: do something with response? e tratar aqui ou no outro?
        SubmitLocationReportResponse response = serverStub.submitLocationReport(request);
    }

    public static LocationReport obtainLocationReport(Long epoch, LocationServerGrpc.LocationServerBlockingStub serverStub)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchPaddingException,
            BadPaddingException, IllegalBlockSizeException, IOException, InvalidKeySpecException,
            InvalidAlgorithmParameterException {

        LocationQuery locationQuery = LocationQuery
                .newBuilder()
                .setUserId(Client.getInstance().getUser().getId())
                .setEpoch(epoch)
                .build();

        byte[] signature = sign(locationQuery.toByteArray(), Client.getInstance().getPrivKey());

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
        byte[] decryptedMessage = symmetricDecrypt(response.getEncryptedSignedLocationReport().toByteArray(), key, iv);

        SignedLocationReport signedLocationReport = SignedLocationReport.parseFrom(decryptedMessage);

        LocationReport report = signedLocationReport.getLocationReport();

        if(!verifySignature(getServerPublicKey(1), report.toByteArray(),
                signedLocationReport.getSignedLocationReport().toByteArray())){

            throw new InvalidParameterException("Invalid location report server signature");
        }

        return report;
    }

}
