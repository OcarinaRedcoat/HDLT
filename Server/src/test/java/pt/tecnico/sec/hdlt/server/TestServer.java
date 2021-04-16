package pt.tecnico.sec.hdlt.server;

import com.google.protobuf.util.JsonFormat;
import org.junit.Before;
import org.junit.Test;
import pt.tecnico.sec.hdlt.communication.SignedLocationReport;
import pt.tecnico.sec.hdlt.server.bll.LocationBL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class TestServer {

    @Before
    public void deleteServerReports(){
        Path fileToDeletePath = Paths.get("../Server/src/main/resources/server_1.txt");
        try {
            Files.deleteIfExists(fileToDeletePath);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Test
    public void testIncorrectNumberOfProofs() {

        try {
            LocationBL locationBL = new LocationBL(1, 2);

            Path path = Paths.get("../Server/src/test/resources/2_proofs.txt");
            String l = Files.readString(path).split("\n")[0];

            SignedLocationReport.Builder builder = SignedLocationReport.newBuilder();
            JsonFormat.parser().merge(l, builder);
            SignedLocationReport report = builder.build();

            locationBL.handleSubmitLocationReport(report);

            fail();
        } catch (Exception e) {
            assertEquals("Invalid number of proofs", e.getMessage());
        }
    }

    @Test
    public void testIncorrectUserSignature() {

        try {
            LocationBL locationBL = new LocationBL(1, 1);

            Path path = Paths.get("../Server/src/test/resources/incorrect_user_signature.txt");
            String l = Files.readString(path).split("\n")[0];

            SignedLocationReport.Builder builder = SignedLocationReport.newBuilder();
            JsonFormat.parser().merge(l, builder);
            SignedLocationReport report = builder.build();

            locationBL.handleSubmitLocationReport(report);

            fail();
        } catch (Exception e) {
            assertEquals("Invalid location information signature", e.getMessage());
        }
    }

    @Test
    public void testSubmitRepeatedReport() {
        SignedLocationReport report = null;

        try {
            LocationBL locationBL = new LocationBL(1, 1);

            Path path = Paths.get("../Server/src/test/resources/2_proofs.txt");
            String l = Files.readString(path).split("\n")[0];

            SignedLocationReport.Builder builder = SignedLocationReport.newBuilder();
            JsonFormat.parser().merge(l, builder);
            report = builder.build();

            locationBL.handleSubmitLocationReport(report);
            locationBL.handleSubmitLocationReport(report);

            fail();
        } catch (Exception e) {
            assertNotEquals(null, report);
            assertEquals("Repeated location report for user: " +
                            report.getLocationReport().getLocationInformation().getUserId() +
                            " and epoch: " +
                            report.getLocationReport().getLocationInformation().getEpoch(), e.getMessage());
        }
    }

//    @Test
//    public void testSubmitRepeatedLocationProofs() {
//
//        try {
//            LocationBL locationBL = new LocationBL(1, 1);
//
//            Path path = Paths.get("../Server/src/test/resources/repeated_proofs.txt");
//            String l = Files.readString(path).split("\n")[0];
//
//            SignedLocationReport.Builder builder = SignedLocationReport.newBuilder();
//            JsonFormat.parser().merge(l, builder);
//            SignedLocationReport report = builder.build();
//
//            locationBL.handleSubmitLocationReport(report);
//
//            fail();
//        } catch (Exception e) {
//            assertEquals("Invalid location information signature", e.getMessage());
//        }
//    }
}
