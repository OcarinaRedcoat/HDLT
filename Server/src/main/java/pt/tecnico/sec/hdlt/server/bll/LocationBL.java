package pt.tecnico.sec.hdlt.server.bll;

import com.google.protobuf.ByteString;
import pt.tecnico.sec.hdlt.FileUtils;
import pt.tecnico.sec.hdlt.communication.*;
import pt.tecnico.sec.hdlt.crypto.CryptographicOperations;
import pt.tecnico.sec.hdlt.server.entities.LocationReportKey;
import pt.tecnico.sec.hdlt.server.utils.ReadFile;
import pt.tecnico.sec.hdlt.server.utils.WriteQueue;

import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class LocationBL {

    private final WriteQueue<LocationReport> writeQueue;
    private final ConcurrentHashMap<LocationReportKey, LocationReport> locationReports;
//    private final PublicKey publicKey;
    private final PrivateKey privateKey;

    public LocationBL(int serverId) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        Path filePath = Paths.get("Server_" + serverId + ".txt");
        this.writeQueue = new WriteQueue<>(filePath);
        this.locationReports = ReadFile.createReportsMap(filePath);
//        this.publicKey = FileUtils.getServerPublicKey(serverId);
        this.privateKey = FileUtils.getServerPrivateKey(serverId);
    }

    public void submitLocationReport(SubmitLocationReportRequest request) throws Exception {
        byte[] reportBytes = decryptRequest(
                request.getEncryptedSignedLocationReport().toByteArray(),
                decryptKey(request.getKey().toByteArray()),
                request.getIv().toByteArray());

        SignedLocationReport sReport = SignedLocationReport.parseFrom(reportBytes);
        LocationReport report = sReport.getLocationReport();
        LocationInformation information = report.getLocationInformation();

        if (!verifySignature(information.getUserId(), report.toByteArray(), sReport.getSignedLocationReport().toByteArray())) {
            throw new InvalidParameterException("Invalid location information signature");
        }

        HashSet<Integer> witnessIds = new HashSet<>();

        // TODO defined f as 4. Not sure.
        if (report.getLocationProofList().size() < 1) {
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

            if (verifyLocationProof(information, lProof)) {
                throw new InvalidParameterException("Invalid location proof");
            }
        }

        LocationReportKey key = new LocationReportKey(information.getUserId(), information.getEpoch());
        if (!this.locationReports.containsKey(key)) {
            this.locationReports.put(key, report);
            this.writeQueue.write(report);
        }
    }

    public ObtainLocationReportResponse obtainLocationReport(ObtainLocationReportRequest request) throws Exception {
        byte[] secretKey = decryptKey(request.getKey().toByteArray());

        byte[] queryBytes = decryptRequest(
                request.getEncryptedSignedLocationQuery().toByteArray(),
                secretKey,
                request.getIv().toByteArray());

        SignedLocationQuery sLocationQuery = SignedLocationQuery.parseFrom(queryBytes);
        LocationQuery locationQuery = sLocationQuery.getLocationQuery();

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

        LocationReport report = this.locationReports.get(key);

        IvParameterSpec iv = CryptographicOperations.generateIv();

        SignedLocationReport signedReport = SignedLocationReport.newBuilder()
                .setLocationReport(report)
                .setSignedLocationReport(ByteString.copyFrom(CryptographicOperations.sign(report.toByteArray(), this.privateKey)))
                .build();

        return ObtainLocationReportResponse.newBuilder()
                .setEncryptedSignedLocationReport(ByteString.copyFrom(encryptResponse(signedReport.toByteArray(), secretKey, iv)))
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

        if (!verifyHaSignature(usersAtLocationQuery.toByteArray(), sUsersAtLocationQuery.toByteArray())) {
            throw new InvalidParameterException("Invalid users at location query signature");
        }

        ListLocationReport.Builder lLReportBuilder = ListLocationReport.newBuilder();

        for (LocationReport report : this.locationReports.values()) {
            LocationInformation information = report.getLocationInformation();
            if (information.getEpoch() == usersAtLocationQuery.getEpoch() &&
                    information.getPosition().getX() == usersAtLocationQuery.getPos().getX() &&
                    information.getPosition().getY() == usersAtLocationQuery.getPos().getY()) {

                lLReportBuilder.addLocationReport(report);
            }
        }

        ListLocationReport lLReport = lLReportBuilder.build();

        SignedListLocationReport sLLReport = SignedListLocationReport.newBuilder()
                .setListLocationReport(lLReport)
                .setSignature(ByteString.copyFrom(CryptographicOperations.sign(lLReport.toByteArray(), this.privateKey)))
                .build();

        IvParameterSpec iv = CryptographicOperations.generateIv();

        return ObtainUsersAtLocationResponse.newBuilder()
                .setEncryptedSignedLocationReport(ByteString.copyFrom(encryptResponse(sLLReport.toByteArray(), secretKey, iv)))
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
                Math.abs(lInfo.getPosition().getX() - lProof.getPosition().getX()) <= 10 &&
                Math.abs(lInfo.getPosition().getY() - lProof.getPosition().getY()) <= 10;
    }

    public void terminateWriteQueue() throws InterruptedException {
        this.writeQueue.terminate();
    }
}
