package pt.tecnico.sec.hdlt.server.bll;

import com.google.protobuf.ByteString;
import com.sun.jdi.InternalException;
import pt.tecnico.sec.hdlt.utils.FileUtils;
import pt.tecnico.sec.hdlt.utils.GeneralUtils;
import pt.tecnico.sec.hdlt.communication.*;
import pt.tecnico.sec.hdlt.utils.CryptographicUtils;
import pt.tecnico.sec.hdlt.server.entities.LocationReportKey;
import pt.tecnico.sec.hdlt.server.utils.MessageWriteQueue;
import pt.tecnico.sec.hdlt.server.utils.NonceWriteQueue;
import pt.tecnico.sec.hdlt.server.utils.ReadFile;

import javax.crypto.spec.IvParameterSpec;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LocationBL {

    private final MessageWriteQueue messageWriteQueue;
    private final NonceWriteQueue nonceWriteQueue;
    private final ConcurrentHashMap<LocationReportKey, SignedLocationReport> locationReports;
    private final Set<String> nonceSet;
    private final PrivateKey privateKey;
    private final int numberByzantineUsers;

    public LocationBL(int serverId, String serverPwd) throws Exception {
        Path messageFilePath = Paths.get("../Server/src/main/resources/server_" + serverId + ".txt");
        Path nonceFilePath = Paths.get("../Server/src/main/resources/server_" + serverId + "_nonce.txt");

        this.messageWriteQueue = new MessageWriteQueue(messageFilePath);
        this.nonceWriteQueue = new NonceWriteQueue(nonceFilePath);

        this.locationReports = ReadFile.createReportsMap(messageFilePath);
        this.nonceSet = ReadFile.createNonceSet(nonceFilePath);

        this.privateKey = CryptographicUtils.getServerPrivateKey(serverId, serverPwd);
        this.numberByzantineUsers = GeneralUtils.F;
    }

    public SubmitLocationReportResponse submitLocationReport(SubmitLocationReportRequest request) throws Exception {
        // Authenticate request
        byte[] key = decryptKey(request.getKey().toByteArray(), this.privateKey);
        byte[] reportBytes = decryptRequest(request.getEncryptedSignedLocationReport().toByteArray(), key, request.getIv().toByteArray());
        SignedLocationReport signedReport = SignedLocationReport.parseFrom(reportBytes);

        if (this.nonceSet.contains(signedReport.getLocationReport().getNonce())) {
            return submitLocationReportResponse(signedReport.getLocationReport().getWts(), "Invalid nonce", key);
        }

        this.nonceSet.add(signedReport.getLocationReport().getNonce());
        this.nonceWriteQueue.write(signedReport.getLocationReport().getNonce());

        if (!verifySignature(signedReport.getLocationReport().getLocationInformation().getUserId(),
                signedReport.getLocationReport().toByteArray(), signedReport.getUserSignature().toByteArray())) {
            throw new InvalidParameterException("Unable to authenticate request");
        }

        return handleSubmitLocationReport(signedReport, key);
    }

    public SubmitLocationReportResponse handleSubmitLocationReport(SignedLocationReport signedReport, byte[] key) throws Exception {
        LocationReport report = signedReport.getLocationReport();
        LocationInformation information = report.getLocationInformation();

        LocationReportKey rKey = new LocationReportKey(information.getUserId(), information.getEpoch());
        if (this.locationReports.containsKey(rKey)) {
            return submitLocationReportResponse(report.getWts(), "Repeated location report", key);
        }

        boolean validReport = true;
        String message = "OK";
        if (!validReport(information, report, this.numberByzantineUsers)) {
            validReport = false;
            message = "Invalid location report";
        }

        signedReport = signedReport.toBuilder().setValid(validReport).build();

        this.locationReports.put(rKey, signedReport);
        this.messageWriteQueue.write(signedReport);

        return submitLocationReportResponse(report.getWts(), message, key);
    }

    private SubmitLocationReportResponse submitLocationReportResponse(int wts, String message, byte[] key) throws Exception {
        Ack ack = Ack.newBuilder().setWts(wts).setMessage(message).build();

        SignedAck signedAck  = SignedAck.newBuilder()
                .setAck(ack)
                .setSignature(ByteString.copyFrom(CryptographicUtils.sign(ack.toByteArray(), this.privateKey)))
                .build();

        IvParameterSpec iv = CryptographicUtils.generateIv();
        return SubmitLocationReportResponse.newBuilder()
                .setEncryptedSignedAck(ByteString.copyFrom(encryptResponse(signedAck.toByteArray(), key, iv)))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .build();
    }

    public ObtainLocationReportResponse obtainLocationReport(ObtainLocationReportRequest request) throws Exception {
        byte[] secretKey = decryptKey(request.getKey().toByteArray(), this.privateKey);

        byte[] queryBytes = decryptRequest(
                request.getEncryptedSignedLocationQuery().toByteArray(),
                secretKey,
                request.getIv().toByteArray());

        SignedLocationQuery sLocationQuery = SignedLocationQuery.parseFrom(queryBytes);
        LocationQuery locationQuery = sLocationQuery.getLocationQuery();

        if (this.nonceSet.contains(locationQuery.getNonce())) {
            throw new InvalidParameterException("Invalid nonce");
        }

        this.nonceSet.add(locationQuery.getNonce());
        this.nonceWriteQueue.write(locationQuery.getNonce());

        boolean verifySignature = locationQuery.getIsHA() ?
                verifyHaSignature(locationQuery.toByteArray(), sLocationQuery.getSignature().toByteArray()) :
                verifySignature(locationQuery.getUserId(), locationQuery.toByteArray(), sLocationQuery.getSignature().toByteArray());

        if (!verifySignature) {
            throw new InvalidParameterException("Invalid location query signature");
        }

        LocationReportKey key = new LocationReportKey(locationQuery.getUserId(), locationQuery.getEpoch());

        SignedLocationReport report = this.locationReports.get(key);

        SignedLocationReportRid signedLocationReportRid = SignedLocationReportRid.newBuilder()
                .setSignedLocationReport(report)
                .setRid(locationQuery.getRid())
                .build();

        ServerSignedSignedLocationReportRid signedSignedLocationReportRid = ServerSignedSignedLocationReportRid.newBuilder()
                .setSignedLocationReportRid(signedLocationReportRid)
                .setServerSignature(ByteString.copyFrom(CryptographicUtils.sign(signedLocationReportRid.toByteArray(), this.privateKey)))
                .build();

        IvParameterSpec iv = CryptographicUtils.generateIv();

        return ObtainLocationReportResponse.newBuilder()
                .setEncryptedServerSignedSignedLocationReportRid(ByteString.copyFrom(encryptResponse(signedSignedLocationReportRid.toByteArray(), secretKey, iv)))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .build();
    }

    public ObtainUsersAtLocationResponse obtainUsersAtLocation(ObtainUsersAtLocationRequest request) throws Exception {
        byte[] secretKey = decryptKey(request.getKey().toByteArray(), this.privateKey);

        byte[] queryBytes = decryptRequest(
                request.getEncryptedSignedUsersAtLocationQuery().toByteArray(),
                secretKey,
                request.getIv().toByteArray());

        SignedUsersAtLocationQuery sUsersAtLocationQuery = SignedUsersAtLocationQuery.parseFrom(queryBytes);
        UsersAtLocationQuery usersAtLocationQuery = sUsersAtLocationQuery.getUsersAtLocationQuery();

        if (this.nonceSet.contains(usersAtLocationQuery.getNonce())) {
            throw new InvalidParameterException("Invalid nonce");
        }

        this.nonceSet.add(usersAtLocationQuery.getNonce());
        this.nonceWriteQueue.write(usersAtLocationQuery.getNonce());

        if (!verifyHaSignature(usersAtLocationQuery.toByteArray(), sUsersAtLocationQuery.getSignature().toByteArray())) {
            throw new InvalidParameterException("Invalid users at location query signature");
        }

        SignedLocationReportList.Builder builder = SignedLocationReportList.newBuilder();

        for (SignedLocationReport report : this.locationReports.values()) {
            LocationInformation information = report.getLocationReport().getLocationInformation();
            if (information.getEpoch() == usersAtLocationQuery.getEpoch() &&
                    information.getPosition().getX() == usersAtLocationQuery.getPos().getX() &&
                    information.getPosition().getY() == usersAtLocationQuery.getPos().getY()) {

                builder.addSignedLocationReportList(report);
            }
        }

        SignedLocationReportList signedLocationReportList = builder.setRid(usersAtLocationQuery.getRid()).build();

        ServerSignedSignedLocationReportList serverSignedSignedLocationReportList = ServerSignedSignedLocationReportList
                .newBuilder()
                .setSignedLocationReportList(signedLocationReportList)
                .setServerSignature(ByteString.copyFrom(CryptographicUtils.sign(signedLocationReportList.toByteArray(), this.privateKey)))
                .build();

        IvParameterSpec iv = CryptographicUtils.generateIv();

        return ObtainUsersAtLocationResponse.newBuilder()
                .setEncryptedSignedLocationReportList(
                        ByteString.copyFrom(
                                encryptResponse(serverSignedSignedLocationReportList.toByteArray(), secretKey, iv)))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .build();
    }

    public RequestMyProofsResponse requestMyProofs(RequestMyProofsRequest request) throws Exception {
        byte[] secretKey = decryptKey(request.getKey().toByteArray(), this.privateKey);

        byte[] queryBytes = decryptRequest(
                request.getEncryptedSignedProofsQuery().toByteArray(),
                secretKey,
                request.getIv().toByteArray());

        SignedProofsQuery signedProofsQuery = SignedProofsQuery.parseFrom(queryBytes);
        ProofsQuery proofsQuery = signedProofsQuery.getProofsQuery();

        if (this.nonceSet.contains(proofsQuery.getNonce())) {
            throw new InvalidParameterException("Invalid nonce");
        }

        this.nonceSet.add(proofsQuery.getNonce());
        this.nonceWriteQueue.write(proofsQuery.getNonce());

        if (!verifyHaSignature(proofsQuery.toByteArray(), signedProofsQuery.getSignature().toByteArray())) {
            throw new InvalidParameterException("Invalid signature");
        }

        Proofs.Builder builder = Proofs.newBuilder();

        for (SignedLocationReport report : this.locationReports.values()) {
            if (!proofsQuery.getEpochsList().contains(report.getLocationReport().getLocationInformation().getEpoch())) {
                continue;
            }

            for (SignedLocationProof sProof : report.getLocationReport().getLocationProofList()) {
                if (sProof.getLocationProof().getWitnessId() == proofsQuery.getUserId()) {
                    builder.addLocationProof(sProof);
                }
            }
        }

        Proofs proofs = builder.setRid(proofsQuery.getRid()).build();

        ServerSignedProofs serverSignedProofs = ServerSignedProofs.newBuilder()
                .setProofs(proofs)
                .setServerSignature(ByteString.copyFrom(CryptographicUtils.sign(proofs.toByteArray(), this.privateKey)))
                .build();

        IvParameterSpec iv = CryptographicUtils.generateIv();

        return RequestMyProofsResponse.newBuilder()
                .setEncryptedServerSignedProofs(ByteString.copyFrom(encryptResponse(serverSignedProofs.toByteArray(), secretKey, iv)))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .build();
    }

    private byte[] decryptKey(byte[] key, PrivateKey privateKey) throws Exception {
        return CryptographicUtils.asymmetricDecrypt(key, privateKey);
    }

    private byte[] decryptRequest(byte[] request, byte[] key, byte[] iv) throws Exception {
        return CryptographicUtils.symmetricDecrypt(request,
                CryptographicUtils.convertToSymmetricKey(key), new IvParameterSpec(iv));
    }

    private byte[] encryptResponse(byte[] data, byte[] secretKey, IvParameterSpec iv) throws Exception {
        return CryptographicUtils.symmetricEncrypt(data, CryptographicUtils.convertToSymmetricKey(secretKey), iv);
    }

    private boolean validReport(LocationInformation information, LocationReport report, int numberByzantineUsers) throws Exception {

        HashSet<Integer> witnessIds = new HashSet<>();

        if (report.getLocationProofList().size() <= numberByzantineUsers) {
            return false;
        }

        for (SignedLocationProof sProof : report.getLocationProofList()) {
            LocationProof lProof = sProof.getLocationProof();

            if (witnessIds.contains(lProof.getWitnessId())) {
                return false;
            }

            witnessIds.add(lProof.getWitnessId());

            if (!verifySignature(lProof.getWitnessId(), lProof.toByteArray(), sProof.getSignature().toByteArray())) {
                return false;
            }

            if (!verifyLocationProof(information, lProof)) {
                return false;
            }
        }

        return true;
    }

    private boolean verifySignature(int userId, byte[] message, byte[] signature) throws Exception {
        return CryptographicUtils.verifySignature(FileUtils.getUserPublicKey(userId), message, signature);
    }

    private boolean verifyLocationProof(LocationInformation lInfo, LocationProof lProof) {
        return lInfo.getUserId() == lProof.getProverId() &&
                lInfo.getEpoch() == lProof.getEpoch() &&
                Math.abs(lInfo.getPosition().getX() - lProof.getPosition().getX()) <= 15 &&
                Math.abs(lInfo.getPosition().getY() - lProof.getPosition().getY()) <= 15;
    }

    private boolean verifyHaSignature(byte[] message, byte[] signature) throws Exception {
        return CryptographicUtils.verifySignature(FileUtils.getHAPublicKey(), message, signature);
    }

    public void terminateMessageWriteQueue() throws InterruptedException {
        this.messageWriteQueue.terminate();
    }

    public void terminateNonceWriteQueue() throws InterruptedException {
        this.nonceWriteQueue.terminate();
    }
}
