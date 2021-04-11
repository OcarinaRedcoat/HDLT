package pt.tecnico.sec.hdlt.server.bll;

import com.google.protobuf.InvalidProtocolBufferException;
import pt.tecnico.sec.hdlt.communication.LocationInformation;
import pt.tecnico.sec.hdlt.communication.LocationProof;
import pt.tecnico.sec.hdlt.communication.LocationReport;
import pt.tecnico.sec.hdlt.communication.SignedLocationProof;
import pt.tecnico.sec.hdlt.server.entities.LocationReportKey;
import pt.tecnico.sec.hdlt.server.utils.ReadFile;
import pt.tecnico.sec.hdlt.server.utils.WriteQueue;

import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

public class LocationBL {

    private final WriteQueue<LocationReport> writeQueue;
    private final ConcurrentHashMap<LocationReportKey, LocationReport> locationReports;

    public LocationBL(Path filePath) {
        this.writeQueue = new WriteQueue<>(filePath);
        this.locationReports = ReadFile.createReportsMap(filePath);
    }

    public void submitLocationReport(byte[] encryptedSignedLocationReport, int userId) throws InterruptedException, InvalidProtocolBufferException {
        byte[] reportBytes = decryptedLocationReport(encryptedSignedLocationReport);

        LocationReport locationReport = LocationReport.parseFrom(reportBytes);

        if (verifySignature(locationReport.getLocationInformationSignature().toByteArray())) {
            throw new InvalidParameterException("Invalid location information signature");
        }

        LocationInformation information = locationReport.getLocationInformation();

        if (information.getUserId() != userId) {
            throw new InvalidParameterException("Invalid user Id");
        }

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

        this.locationReports.put(new LocationReportKey(information.getUserId(), information.getEpoch()), locationReport);
        this.writeQueue.write(locationReport);
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
