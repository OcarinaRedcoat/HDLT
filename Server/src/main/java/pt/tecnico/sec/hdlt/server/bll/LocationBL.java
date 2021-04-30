package pt.tecnico.sec.hdlt.server.bll;

import com.google.protobuf.ByteString;
import pt.tecnico.sec.hdlt.FileUtils;
import pt.tecnico.sec.hdlt.GeneralUtils;
import pt.tecnico.sec.hdlt.communication.*;
import pt.tecnico.sec.hdlt.crypto.CryptographicOperations;
import pt.tecnico.sec.hdlt.server.entities.LocationReportKey;
import pt.tecnico.sec.hdlt.server.utils.MessageWriteQueue;
import pt.tecnico.sec.hdlt.server.utils.NonceWriteQueue;
import pt.tecnico.sec.hdlt.server.utils.ReadFile;

import javax.crypto.spec.IvParameterSpec;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
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

        this.privateKey = CryptographicOperations.getServerPrivateKey(serverId, serverPwd);
        this.numberByzantineUsers = GeneralUtils.F;
    }

    public void submitLocationReport(SubmitLocationReportRequest request) throws Exception {
        byte[] reportBytes = decryptRequest(
                request.getEncryptedSignedLocationReport().toByteArray(),
                decryptKey(request.getKey().toByteArray()),
                request.getIv().toByteArray());

        SignedLocationReport signedLocationReport = SignedLocationReport.parseFrom(reportBytes);
        handleSubmitLocationReport(signedLocationReport);
    }

    public void handleSubmitLocationReport(SignedLocationReport signedLocationReport) throws Exception {
        LocationReport report = signedLocationReport.getLocationReport();
        LocationInformation information = report.getLocationInformation();

        if (this.nonceSet.contains(report.getNonce())) {
            throw new InvalidParameterException("Invalid nonce");
        }

        this.nonceSet.add(report.getNonce());
        this.nonceWriteQueue.write(report.getNonce());

        LocationReportKey key = new LocationReportKey(information.getUserId(), information.getEpoch());
        if (this.locationReports.containsKey(key)) {
            throw new InvalidParameterException("Repeated location report for user: " + information.getUserId() + " and epoch: " + information.getEpoch());
        }

        if (!verifySignature(information.getUserId(), report.toByteArray(), signedLocationReport.getUserSignature().toByteArray())) {
            throw new InvalidParameterException("Invalid location information signature");
        }

        HashSet<Integer> witnessIds = new HashSet<>();

        if (report.getLocationProofList().size() <= this.numberByzantineUsers) {
            throw new InvalidParameterException("Invalid number of proofs");
        }

        for (SignedLocationProof sProof : report.getLocationProofList()) {
            LocationProof lProof = sProof.getLocationProof();

            if (witnessIds.contains(lProof.getWitnessId())) {
                throw new InvalidParameterException("Repeated location proof");
            }

            witnessIds.add(lProof.getWitnessId());

            if (!verifySignature(lProof.getWitnessId(), lProof.toByteArray(), sProof.getSignature().toByteArray())) {
                throw new InvalidParameterException("Invalid location proof signature");
            }

            if (!verifyLocationProof(information, lProof)) {
                throw new InvalidParameterException("Invalid location proof");
            }
        }

        this.locationReports.put(key, signedLocationReport);
        this.messageWriteQueue.write(signedLocationReport);
    }

    public ObtainLocationReportResponse obtainLocationReport(ObtainLocationReportRequest request) throws Exception {
        byte[] secretKey = decryptKey(request.getKey().toByteArray());

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
        if (!this.locationReports.containsKey(key)) {
            throw new NoSuchFieldException("No report found for user: " + locationQuery.getUserId() + " at epoch: " + locationQuery.getEpoch());
        }

        SignedLocationReport report = this.locationReports.get(key);

        ServerSignedSignedLocationReport serverSignedSignedLocationReport = ServerSignedSignedLocationReport.newBuilder()
                .setSignedLocationReport(report)
                .setServerSignature(ByteString.copyFrom(CryptographicOperations.sign(report.toByteArray(), this.privateKey)))
                .build();

        IvParameterSpec iv = CryptographicOperations.generateIv();

        return ObtainLocationReportResponse.newBuilder()
                .setEncryptedServerSignedSignedLocationReport(ByteString.copyFrom(encryptResponse(serverSignedSignedLocationReport.toByteArray(), secretKey, iv)))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .build();
    }

    public ObtainUsersAtLocationResponse obtainUsersAtLocation(ObtainUsersAtLocationRequest request) throws Exception {
        byte[] secretKey = decryptKey(request.getKey().toByteArray());

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

        SignedLocationReportList signedLocationReportList = builder.build();

        ServerSignedSignedLocationReportList serverSignedSignedLocationReportList = ServerSignedSignedLocationReportList
                .newBuilder()
                .setSignedLocationReportList(signedLocationReportList)
                .setServerSignature(ByteString.copyFrom(CryptographicOperations.sign(signedLocationReportList.toByteArray(), this.privateKey)))
                .build();

        IvParameterSpec iv = CryptographicOperations.generateIv();

        return ObtainUsersAtLocationResponse.newBuilder()
                .setEncryptedSignedLocationReportList(
                        ByteString.copyFrom(
                                encryptResponse(serverSignedSignedLocationReportList.toByteArray(), secretKey, iv)))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .build();
    }

    private byte[] decryptKey(byte[] key) throws Exception {
        return CryptographicOperations.asymmetricDecrypt(key, this.privateKey);
    }

    private byte[] decryptRequest(byte[] request, byte[] key, byte[] iv) throws Exception {
        return CryptographicOperations.symmetricDecrypt(request,
                CryptographicOperations.convertToSymmetricKey(key), new IvParameterSpec(iv));
    }

    private byte[] encryptResponse(byte[] data, byte[] secretKey, IvParameterSpec iv) throws Exception {
        return CryptographicOperations.symmetricEncrypt(data, CryptographicOperations.convertToSymmetricKey(secretKey), iv);
    }

    private boolean verifySignature(int userId, byte[] message, byte[] signature) throws Exception {
        return CryptographicOperations.verifySignature(FileUtils.getUserPublicKey(userId), message, signature);
    }

    private boolean verifyHaSignature(byte[] message, byte[] signature) throws Exception {
        return CryptographicOperations.verifySignature(FileUtils.getHAPublicKey(), message, signature);
    }

    private boolean verifyLocationProof(LocationInformation lInfo, LocationProof lProof) {
        return lInfo.getUserId() == lProof.getProverId() &&
                lInfo.getEpoch() == lProof.getEpoch() &&
                Math.abs(lInfo.getPosition().getX() - lProof.getPosition().getX()) <= 15 &&
                Math.abs(lInfo.getPosition().getY() - lProof.getPosition().getY()) <= 15;
    }

    public void terminateMessageWriteQueue() throws InterruptedException {
        this.messageWriteQueue.terminate();
    }

    public void terminateNonceWriteQueue() throws InterruptedException {
        this.nonceWriteQueue.terminate();
    }
}
