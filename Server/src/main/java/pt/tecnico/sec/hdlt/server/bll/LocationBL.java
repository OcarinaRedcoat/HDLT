package pt.tecnico.sec.hdlt.server.bll;

import com.google.protobuf.InvalidProtocolBufferException;
import pt.tecnico.sec.hdlt.FileUtils;
import pt.tecnico.sec.hdlt.communication.*;
import pt.tecnico.sec.hdlt.crypto.CryptographicOperations;
import pt.tecnico.sec.hdlt.server.entities.LocationReportKey;
import pt.tecnico.sec.hdlt.server.utils.ReadFile;
import pt.tecnico.sec.hdlt.server.utils.WriteQueue;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class LocationBL {

    private final WriteQueue<LocationReport> writeQueue;
    private final ConcurrentHashMap<LocationReportKey, LocationReport> locationReports;
//    private final PublicKey publicKey;
//    private final PrivateKey privateKey;

    public LocationBL(int serverId) {
        Path filePath = Paths.get("Server" + serverId + ".txt");
        this.writeQueue = new WriteQueue<>(filePath);
        this.locationReports = ReadFile.createReportsMap(filePath);
//        this.publicKey = FileUtils.readPublicKey();
//        this.privateKey = FileUtils.readPrivateKey();
    }

    public void submitLocationReport(byte[] encryptedSignedLocationReport) throws InterruptedException, InvalidProtocolBufferException {
        // TODO decrypt symmetric key

        byte[] reportBytes = decryptedLocationReport(encryptedSignedLocationReport);
        LocationReport locationReport = LocationReport.parseFrom(reportBytes);

        if (!verifySignature(locationReport.getLocationInformationSignature().toByteArray())) {
            throw new InvalidParameterException("Invalid location information signature");
        }

        LocationInformation information = locationReport.getLocationInformation();

        // TODO verify epoch

        HashSet<Integer> witnessIds = new HashSet<>();

        if (locationReport.getLocationProofList().size() < 5) {
            throw new InvalidParameterException("Invalid number of proofs");
        }

        for (SignedLocationProof sProof : locationReport.getLocationProofList()) {
            LocationProof lProof = sProof.getLocationProof();

            if (witnessIds.contains(lProof.getWitnessId())) {
                throw new InvalidParameterException("Repeated location proof");
            }

            witnessIds.add(lProof.getWitnessId());

            if (verifySignature(sProof.getSignature().toByteArray())) {
                throw new InvalidParameterException("Invalid location proof signature");
            }

            if (verifyLocationProof(information, lProof)) {
                throw new InvalidParameterException("Invalid location proof");
            }
        }

        LocationReportKey key = new LocationReportKey(information.getUserId(), information.getEpoch());
        if (!this.locationReports.contains(key)) {
            this.locationReports.put(key, locationReport);
            this.writeQueue.write(locationReport);
        }
    }

    public ObtainLocationReportResponse obtainLocationReport(byte[] encryptedSignedLocationQuery) throws InvalidProtocolBufferException, NoSuchFieldException {
        // TODO decrypt symmetric key

        byte[] reportBytes = decryptedLocationReport(encryptedSignedLocationQuery);
        SignedLocationQuery sLocationQuery = SignedLocationQuery.parseFrom(reportBytes);
        LocationQuery locationQuery = sLocationQuery.getLocationQuery();

        // TODO verify signature
        if (!verifySignature(sLocationQuery.getSignature().toByteArray())) {
            throw new InvalidParameterException("Invalid location query signature");
        }

        LocationReportKey key = new LocationReportKey(locationQuery.getUserId(), locationQuery.getEpoch());
        if (!this.locationReports.contains(key)) {
            throw new NoSuchFieldException("No report found for user: " + locationQuery.getUserId() + "at epoch: " + locationQuery.getEpoch());
        }

        LocationReport report = this.locationReports.get(key);
        SignedLocationReport signedReport = SignedLocationReport.newBuilder()
                .setLocationReport(report)
//                .setSignedLocationReport()
//                .setIv()
                .build();

        return ObtainLocationReportResponse.newBuilder()/*.setEncryptedSignedLocationReport()*/.build();
    }

    private byte[] decryptedLocationReport(byte[] encryptedSignedLocationReport) {
        // TODO decrypt
        return new byte[1];
    }

    private boolean verifySignature(byte[] signature) {
        // TODO verify signature
        return true;
    }

    private boolean verifyLocationProof(LocationInformation lInfo, LocationProof lProof) {
        return lInfo.getUserId() == lProof.getProverId() &&
                lInfo.getEpoch() == lProof.getEpoch() &&
                lInfo.getPosition().getX() == lProof.getPosition().getX() &&
                lInfo.getPosition().getY() == lProof.getPosition().getY();
    }

    public void terminateWriteQueue() throws InterruptedException {
        this.writeQueue.terminate();
    }
}
