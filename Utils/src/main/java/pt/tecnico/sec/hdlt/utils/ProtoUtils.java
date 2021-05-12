package pt.tecnico.sec.hdlt.utils;

import com.google.protobuf.ByteString;
import pt.tecnico.sec.hdlt.communication.*;
import pt.tecnico.sec.hdlt.entities.Client;

import javax.crypto.spec.IvParameterSpec;

import java.util.List;

import static pt.tecnico.sec.hdlt.utils.CryptographicUtils.generateNonce;

public class ProtoUtils {

    public static Position buildPosition(Client client, Long epoch){
        return Position.newBuilder()
                .setX(client.getUser().getPositionWithEpoch(epoch).getxPos())
                .setY(client.getUser().getPositionWithEpoch(epoch).getyPos())
                .build();
    }

    public static LocationInformation buildLocationInformation(Client client, Long epoch){
        return LocationInformation
                .newBuilder()
                .setUserId(client.getUser().getId())
                .setEpoch(epoch)
                .setPosition(buildPosition(client, epoch))
                .build();
    }

    public static LocationInformationRequest buildLocationInformationRequest(LocationInformation locationInformation, byte[] signature){
        return LocationInformationRequest
                .newBuilder()
                .setLocationInformation(locationInformation)
                .setSignature(ByteString.copyFrom(signature))
                .build();
    }

    public static LocationQuery buildLocationQuery(Client client, Long epoch, int rid){
        return LocationQuery
                .newBuilder()
                .setUserId(client.getUser().getId())
                .setEpoch(epoch)
                .setNonce(generateNonce())
                .setRid(rid)
                .build();
    }

    public static ProofsQuery buildProofsQuery(Client client, int rid, List<Long> epochs){
        ProofsQuery.Builder builder = ProofsQuery
                .newBuilder()
                .setUserId(client.getUser().getId())
                .setNonce(generateNonce())
                .setRid(rid);

        for (int i = 0; i < epochs.size(); i++) {
            builder.addEpochs(epochs.get(i));
        }

        return builder.build();
    }

    public static SignedLocationQuery buildSignedLocationQuery(LocationQuery locationQuery, byte[] signature){
        return SignedLocationQuery
                .newBuilder()
                .setLocationQuery(locationQuery)
                .setSignature(ByteString.copyFrom(signature))
                .build();
    }

    public static SignedLocationReportWrite buildSignedLocationReportWrite(SignedLocationReport signedLocationReport, int rid, String nonce, Boolean isHa){
        return SignedLocationReportWrite
                .newBuilder()
                .setSignedLocationReport(signedLocationReport)
                .setNonce(nonce)
                .setRid(rid)
                .setIsHa(isHa)
                .build();
    }

    public static SignedProofsQuery buildSignedProofsQuery(ProofsQuery proofsQuery, byte[] signature){
        return SignedProofsQuery
                .newBuilder()
                .setProofsQuery(proofsQuery)
                .setSignature(ByteString.copyFrom(signature))
                .build();
    }

    public static ObtainLocationReportRequest buildObtainLocationReportRequest(byte[] encryptedKey, byte[] encryptedMessage, IvParameterSpec iv){
        return ObtainLocationReportRequest
                .newBuilder()
                .setKey(ByteString.copyFrom(encryptedKey))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .setEncryptedSignedLocationQuery(ByteString.copyFrom(encryptedMessage))
                .build();
    }

    public static RequestMyProofsRequest buildRequestMyProofsRequest(byte[] encryptedKey, byte[] encryptedMessage, IvParameterSpec iv){
        return RequestMyProofsRequest
                .newBuilder()
                .setKey(ByteString.copyFrom(encryptedKey))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .setEncryptedSignedProofsQuery(ByteString.copyFrom(encryptedMessage))
                .build();
    }

    public static SignedLocationReport buildSignedLocationReport(LocationReport report, byte[] signature){
        return SignedLocationReport
                .newBuilder()
                .setLocationReport(report)
                .setUserSignature(ByteString.copyFrom(signature))
                .build();
    }

    public static SubmitLocationReportRequest buildSubmitLocationReportRequest(byte[] encryptedKey, byte[] encryptedMessage, IvParameterSpec iv){
        return SubmitLocationReportRequest
                .newBuilder()
                .setKey(ByteString.copyFrom(encryptedKey))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .setEncryptedAuthenticatedSignedLocationReportWrite(ByteString.copyFrom(encryptedMessage))
                .build();
    }

    public static AuthenticatedSignedLocationReportWrite buildAuthenticatedSignedLocationReportWrite(SignedLocationReportWrite signedLocationReportWrite, byte[] signature){
        return AuthenticatedSignedLocationReportWrite
                .newBuilder()
                .setSignedLocationReportWrite(signedLocationReportWrite)
                .setSignature(ByteString.copyFrom(signature))
                .build();
    }

}
