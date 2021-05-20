package pt.tecnico.sec.hdlt.utils;

import com.google.protobuf.ByteString;
import pt.tecnico.sec.hdlt.communication.*;
import pt.tecnico.sec.hdlt.entities.Client;

import javax.crypto.spec.IvParameterSpec;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import static pt.tecnico.sec.hdlt.utils.CryptographicUtils.generateNonce;
import static pt.tecnico.sec.hdlt.utils.CryptographicUtils.isValidPoW;

public class ProtoUtils {

    public static Position buildPosition(Client client, Long epoch){
        return Position.newBuilder()
                .setX(client.getUser().getPositionWithEpoch(epoch).getxPos())
                .setY(client.getUser().getPositionWithEpoch(epoch).getyPos())
                .build();
    }

    public static LocationProof buildLocationProof(Client client, Long epoch, int requesterId){
        return LocationProof
                .newBuilder()
                .setProverId(requesterId)
                .setWitnessId(client.getUser().getId())
                .setEpoch(epoch)
                .setPosition(buildPosition(client, epoch))
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

    public static LocationQuery buildLocationQuery(int clientId, Long epoch, int rid, Boolean isHa)
            throws IOException, NoSuchAlgorithmException {
        LocationQuery locationQuery;
        System.out.println("Generating PoW.");
        do{
            locationQuery = LocationQuery
                    .newBuilder()
                    .setUserId(clientId)
                    .setEpoch(epoch)
                    .setIsHA(isHa)
                    .setNonce(generateNonce())
                    .setRid(rid)
                    .build();
        } while (!isValidPoW(locationQuery));
        System.out.println("PoW Generated.");
        return locationQuery;
    }

    public static Echo buildEcho(int serverId, SignedLocationReport signedLocationReport)
            throws IOException, NoSuchAlgorithmException {
        Echo echo;
        do{
            echo = Echo.newBuilder()
                    .setSignedLocationReport(signedLocationReport)
                    .setServerId(serverId)
                    .setNonce(generateNonce())
                    .build();
        } while (!isValidPoW(echo));
        return echo;
    }

    public static Ready buildReady(int serverId, SignedLocationReport signedLocationReport)
            throws IOException, NoSuchAlgorithmException {
        Ready ready;
        do{
            ready = Ready.newBuilder()
                    .setSignedLocationReport(signedLocationReport)
                    .setServerId(serverId)
                    .setNonce(generateNonce())
                    .build();
        } while (!isValidPoW(ready));
        return ready;
    }

    public static ProofsQuery buildProofsQuery(Client client, int rid, List<Long> epochs)
            throws IOException, NoSuchAlgorithmException {
        ProofsQuery proofsQuery;
        ProofsQuery.Builder builder;
        System.out.println("Generating PoW.");
        do{
            builder = ProofsQuery
                    .newBuilder()
                    .setUserId(client.getUser().getId())
                    .setNonce(generateNonce())
                    .setRid(rid);

            for (int i = 0; i < epochs.size(); i++) {
                builder.addEpochs(epochs.get(i));
            }

            proofsQuery = builder.build();
        } while (!isValidPoW(proofsQuery));
        System.out.println("PoW Generated.");
        return proofsQuery;
    }

    public static SignedLocationQuery buildSignedLocationQuery(LocationQuery locationQuery, byte[] signature){
        return SignedLocationQuery
                .newBuilder()
                .setLocationQuery(locationQuery)
                .setSignature(ByteString.copyFrom(signature))
                .build();
    }

    public static SignedLocationProof buildSignedLocationProof(LocationProof proof, byte[] signature){
        return SignedLocationProof.newBuilder()
                .setLocationProof(proof)
                .setSignature(ByteString.copyFrom(signature))
                .build();
    }

    public static SignedLocationReportWrite buildSignedLocationReportWrite(SignedLocationReport signedLocationReport, int rid, Boolean isHa)
            throws IOException, NoSuchAlgorithmException {
        SignedLocationReportWrite signedLocationReportWrite;
        System.out.println("Generating PoW.");
        do{
            signedLocationReportWrite = SignedLocationReportWrite
                    .newBuilder()
                    .setSignedLocationReport(signedLocationReport)
                    .setNonce(generateNonce())
                    .setRid(rid)
                    .setIsHa(isHa)
                    .build();
        } while (!isValidPoW(signedLocationReportWrite));
        System.out.println("PoW Generated.");
        return signedLocationReportWrite;
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

    public static Position buildPosition(long x, long y){
        return Position
                .newBuilder()
                .setX(x)
                .setY(y)
                .build();
    }

    public static UsersAtLocationQuery buildUsersAtLocationQuery(Position pos, long epoch, int rid)
            throws IOException, NoSuchAlgorithmException {
        UsersAtLocationQuery usersAtLocationQuery;
        System.out.println("Generating PoW.");
        do{
            usersAtLocationQuery = UsersAtLocationQuery
                    .newBuilder()
                    .setPos(pos)
                    .setEpoch(epoch)
                    .setNonce(generateNonce())
                    .setRid(rid)
                    .build();
        } while (!isValidPoW(usersAtLocationQuery));
        System.out.println("PoW Generated.");
        return usersAtLocationQuery;
    }

    public static SignedUsersAtLocationQuery buildSignedUsersAtLocationQuery(UsersAtLocationQuery query, byte[] signature){
        return SignedUsersAtLocationQuery
            .newBuilder()
            .setUsersAtLocationQuery(query)
            .setSignature(ByteString.copyFrom(signature))
            .build();
    }

    public static ObtainUsersAtLocationRequest buildObtainUsersAtLocationRequest(byte[] request, IvParameterSpec iv, byte[] key){
        return ObtainUsersAtLocationRequest
            .newBuilder()
            .setEncryptedSignedUsersAtLocationQuery(ByteString.copyFrom(request))
            .setIv(ByteString.copyFrom(iv.getIV()))
            .setKey(ByteString.copyFrom(key))
            .build();
    }
}
