package pt.tecnico.sec.hdlt.haclient.bll;

import com.google.protobuf.ByteString;
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
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

import static pt.tecnico.sec.hdlt.FileUtils.getServerPublicKey;
import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.*;

public class HABL {

    public static SignedLocationReport obtainLocationReport(int userId, Long epoch, LocationServerGrpc.LocationServerBlockingStub serverStub)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchPaddingException,
            BadPaddingException, IllegalBlockSizeException, IOException, InvalidKeySpecException, InvalidAlgorithmParameterException {

        LocationQuery locationQuery = LocationQuery
                .newBuilder()
                .setUserId(userId)
                .setEpoch(epoch)
                .setIsHA(true)
                .build();

        byte[] signature = sign(locationQuery.toByteArray(), HA.getInstance().getPrivateKey());

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
        byte[] decryptedMessage = symmetricDecrypt(
                response.getEncryptedServerSignedSignedLocationReport().toByteArray(),
                key,
                new IvParameterSpec(response.getIv().toByteArray()));

        ServerSignedSignedLocationReport signedSignedLocationReport = ServerSignedSignedLocationReport.parseFrom(decryptedMessage);

        SignedLocationReport report = signedSignedLocationReport.getSignedLocationReport();

        if(!verifySignature(getServerPublicKey(1), signedSignedLocationReport.toByteArray(),
                signedSignedLocationReport.getServerSignature().toByteArray())){
            //TODO: exception
            throw new InvalidKeyException();
        }

        return report;
    }
    /* Params: pos, ep .....
     * Specification: returns a list of users that were at position pos at epoch ep
     */
    public static List<SignedLocationReport> obtainUsersAtLocation(long x, long y, long ep, LocationServerGrpc.LocationServerBlockingStub serverStub)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, InvalidAlgorithmParameterException,
            BadPaddingException, NoSuchPaddingException, IllegalBlockSizeException, IOException, InvalidKeySpecException {
        Position pos = Position.newBuilder()
                .setX(x)
                .setY(y)
                .build();

        UsersAtLocationQuery usersAtLocationQuery = UsersAtLocationQuery
                .newBuilder()
                .setPos(pos)
                .setEpoch(ep)
                .build();

        byte[] signature = sign(usersAtLocationQuery.toByteArray(), HA.getInstance().getPrivateKey());

        SignedUsersAtLocationQuery signedUsersAtLocationQuery = SignedUsersAtLocationQuery
                .newBuilder()
                .setUsersAtLocationQuery(usersAtLocationQuery)
                .setSignature(ByteString.copyFrom(signature))
                .build();

        SecretKey key = generateSecretKey();

        IvParameterSpec iv = generateIv();
        byte[] encryptedMessage = symmetricEncrypt(signedUsersAtLocationQuery.toByteArray(), key, iv);
        byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(1));

        ObtainUsersAtLocationRequest obtainUsersAtLocationRequest = ObtainUsersAtLocationRequest
                .newBuilder()
                .setEncryptedSignedUsersAtLocationQuery(ByteString.copyFrom(encryptedMessage))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .setKey(ByteString.copyFrom(encryptedKey))
                .build();

        ObtainUsersAtLocationResponse response = serverStub.obtainUsersAtLocation(obtainUsersAtLocationRequest);

        byte[] decryptedMessage = symmetricDecrypt(response.getEncryptedSignedLocationReportList().toByteArray(),
                key,
                new IvParameterSpec(response.getIv().toByteArray()));

        ServerSignedSignedLocationReportList signedSignedLocationReportList = ServerSignedSignedLocationReportList.parseFrom(decryptedMessage);
        SignedLocationReportList signedLocationReportList = signedSignedLocationReportList.getSignedLocationReportList();

        List<SignedLocationReport> list = new ArrayList<>(signedLocationReportList.getSignedLocationReportListList());

        if(!verifySignature(getServerPublicKey(1), signedSignedLocationReportList.toByteArray(),
                signedSignedLocationReportList.getServerSignature().toByteArray())){
            //TODO: exception
            throw new InvalidKeyException();
        }

        return list;
    }
}
