package pt.tecnico.sec.hdlt.client.bll;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import io.grpc.StatusRuntimeException;
import pt.tecnico.sec.hdlt.User;
import pt.tecnico.sec.hdlt.client.communication.UserClient;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.*;
import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.symmetricDecrypt;

public class ClientBL {

    private static final Logger logger = Logger.getLogger(UserClient.class.getName());

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

        byte[] signature = sign(request.toByteArray(), Client.getInstance().getPrivKey());

        LocationReport.Builder reportBuilder = LocationReport
                .newBuilder()
                .setLocationInformationSignature(ByteString.copyFrom(signature))
                .setLocationInformation(request);

        for (ClientServerGrpc.ClientServerBlockingStub stub : userStubs) {
            try{
                SignedLocationProof response = stub.requestLocationProof(request);
                reportBuilder.addLocationProof(response);
            } catch (StatusRuntimeException e) {
                logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            }
        }

        return reportBuilder.build();
    }

    public static void submitLocationReport(LocationReport report, LocationServerGrpc.LocationServerBlockingStub serverStub)
            throws NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, NoSuchPaddingException,
            IllegalBlockSizeException {

        SecretKey key = generateSecretKey();
        //TODO IV
        byte[] encryptedMessage = symmetricEncrypt(report.toByteArray(), key);

        //TODO: enviar chave symmetrica encrypted com server public
        SubmitLocationReportRequest request = SubmitLocationReportRequest
                .newBuilder()
                .setUserId(Client.getInstance().getUser().getId())
                .setEncryptedSignedLocationReport(ByteString.copyFrom(encryptedMessage))
                .build();

        //TODO: do something with response? e tratar aqui ou no outro?
        SubmitLocationReportResponse response = serverStub.submitLocationReport(request);
    }

    public static LocationReport obtainLocationReport(Long epoch, LocationServerGrpc.LocationServerBlockingStub serverStub)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchPaddingException,
            BadPaddingException, IllegalBlockSizeException, InvalidProtocolBufferException {

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
        //TODO IV
        byte[] encryptedMessage = symmetricEncrypt(signedLocationQuery.toByteArray(), key);

        //TODO: enviar chave symmetrica encrypted com server public
        ObtainLocationReportRequest request = ObtainLocationReportRequest
                .newBuilder()
                .setEncryptedSignedLocationQuery(ByteString.copyFrom(encryptedMessage))
                .build();

        ObtainLocationReportResponse response = serverStub.obtainLocationReport(request);
        byte[] decryptedMessage = symmetricDecrypt(response.getEncryptedLocationReport().toByteArray(), key);

        //TODO: verify server signature

        LocationReport report = LocationReport.parseFrom(decryptedMessage);
        return report;
    }

}
