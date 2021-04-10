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
import java.util.concurrent.ConcurrentHashMap;

public class LocationBL {

    private WriteQueue<LocationReport> writeQueue;
    private ConcurrentHashMap<LocationReportKey, LocationReport> locationReports;

    public LocationBL(Path filePath) {
        this.writeQueue = new WriteQueue<>(filePath);
        this.locationReports = ReadFile.createReportsMap(filePath);
    }

    public void submitLocationReport(byte[] encryptedSignedLocationReport) throws InterruptedException, InvalidProtocolBufferException {
        byte[] reportBytes = getDecryptedLocationReport(encryptedSignedLocationReport);
        LocationReport locationReport = LocationReport.parseFrom(reportBytes);

        LocationInformation information = locationReport.getLocationInformation();

        // TODO verify number of proofs

        for (SignedLocationProof sProof : locationReport.getLocationProofList()) {
            LocationProof lProof = sProof.getLocationProof();

            // TODO verify that there are no 2 proofs from the same witness

            // TODO verify signature

            if (verifyLocationProof(information, lProof)) {
                // TODO throw exception due to invalid signature
            }
        }

        this.locationReports.put(new LocationReportKey(information.getUserId(), information.getEpoch()), locationReport);
        this.writeQueue.write(locationReport);
    }

    private byte[] getDecryptedLocationReport(byte[] encryptedSignedLocationReport) {
        return new byte[1];
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
