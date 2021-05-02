package pt.tecnico.sec.hdlt.server;

import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;
import org.junit.Before;
import org.junit.Test;
import pt.tecnico.sec.hdlt.FileUtils;
import pt.tecnico.sec.hdlt.communication.*;
import pt.tecnico.sec.hdlt.crypto.CryptographicOperations;
import pt.tecnico.sec.hdlt.server.bll.LocationBL;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class TestServer {

//    @Before
//    public void deleteServerReports(){
//        Path fileToDeletePath = Paths.get("../Server/src/main/resources/server_1.txt");
//        try {
//            Files.deleteIfExists(fileToDeletePath);
//        } catch (IOException e) {
//            System.err.println(e.getMessage());
//        }
//    }
//
//    @Test
//    public void testIncorrectNumberOfProofs() {
//
//        try {
//            LocationBL locationBL = new LocationBL(1, "server_1");
//
//            Path path = Paths.get("../Server/src/test/resources/2_proofs.txt");
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
//            assertEquals("Invalid number of proofs", e.getMessage());
//        }
//    }
//
//    @Test
//    public void testIncorrectUserSignature() {
//
//        try {
//            LocationBL locationBL = new LocationBL(1, "server_1");
//
//            Path path = Paths.get("../Server/src/test/resources/incorrect_user_signature.txt");
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
//
//    @Test
//    public void testSubmitRepeatedReport() {
//        SignedLocationReport report = null;
//
//        try {
//            LocationBL locationBL = new LocationBL(1, "server_1");
//
//            Path path = Paths.get("../Server/src/test/resources/2_proofs.txt");
//            String l = Files.readString(path).split("\n")[0];
//
//            SignedLocationReport.Builder builder = SignedLocationReport.newBuilder();
//            JsonFormat.parser().merge(l, builder);
//            report = builder.build();
//
//            locationBL.handleSubmitLocationReport(report);
//            locationBL.handleSubmitLocationReport(report);
//
//            fail();
//        } catch (Exception e) {
//            assertNotEquals(null, report);
//            assertEquals("Repeated location report for user: " +
//                            report.getLocationReport().getLocationInformation().getUserId() +
//                            " and epoch: " +
//                            report.getLocationReport().getLocationInformation().getEpoch(), e.getMessage());
//        }
//    }

//    TODO Re-fazer estes testes
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
//            builder.setUserSignature(ByteString.copyFrom(CryptographicOperations.sign(builder.getLocationReport().toByteArray(), FileUtils.getUserPrivateKey(1))));
//
//            SignedLocationReport report = builder.build();
//
//            locationBL.handleSubmitLocationReport(report);
//
//            fail();
//        } catch (Exception e) {
//            assertEquals("Repeated location proof", e.getMessage());
//        }
//    }
//
//    @Test
//    public void testSubmitIncorrectProofSignature() {
//
//        try {
//            LocationBL locationBL = new LocationBL(1, 1);
//
//            Path path = Paths.get("../Server/src/test/resources/incorrect_proof_signature.txt");
//            String l = Files.readString(path).split("\n")[0];
//
//            SignedLocationReport.Builder builder = SignedLocationReport.newBuilder();
//            JsonFormat.parser().merge(l, builder);
//            builder.setUserSignature(ByteString.copyFrom(CryptographicOperations.sign(builder.getLocationReport().toByteArray(), FileUtils.getUserPrivateKey(1))));
//
//            SignedLocationReport report = builder.build();
//
//            locationBL.handleSubmitLocationReport(report);
//
//            fail();
//        } catch (Exception e) {
//            assertEquals("Invalid location proof signature", e.getMessage());
//        }
//    }
//
//    @Test
//    public void testSubmitProofWithIncorrectId() {
//
//        try {
//            LocationBL locationBL = new LocationBL(1, 1);
//
//            Path path = Paths.get("../Server/src/test/resources/incorrect_proof_id.txt");
//            String l = Files.readString(path).split("\n")[0];
//
//            SignedLocationReport.Builder builder = SignedLocationReport.newBuilder();
//            JsonFormat.parser().merge(l, builder);
//
//            LocationProof locationProof = LocationProof.newBuilder()
//                    .setWitnessId(3)
//                    .setProverId(2)
//                    .setEpoch(2)
//                    .setPosition(Position.newBuilder().setX(72).setY(32).build())
//                    .build();
//
//            SignedLocationProof signedLocationProof = SignedLocationProof.newBuilder().
//                    setLocationProof(locationProof)
//                    .setSignature(ByteString.copyFrom(CryptographicOperations.sign(locationProof.toByteArray(), FileUtils.getUserPrivateKey(3))))
//                    .build();
//
//            LocationReport.Builder locationReportBuilder = builder.getLocationReport().toBuilder();
//            locationReportBuilder.addLocationProof(signedLocationProof);
//            builder.setLocationReport(locationReportBuilder.build());
//
//            builder.setUserSignature(ByteString.copyFrom(CryptographicOperations.sign(builder.getLocationReport().toByteArray(), FileUtils.getUserPrivateKey(1))));
//
//            SignedLocationReport signedLocationReport = builder.build();
//
//            locationBL.handleSubmitLocationReport(signedLocationReport);
//
//            fail();
//        } catch (Exception e) {
//            assertEquals("Invalid location proof", e.getMessage());
//        }
//    }
//
//    @Test
//    public void testSubmitProofWithIncorrectEpoch() {
//
//        try {
//            LocationBL locationBL = new LocationBL(1, 1);
//
//            Path path = Paths.get("../Server/src/test/resources/incorrect_proof_epoch.txt");
//            String l = Files.readString(path).split("\n")[0];
//
//            SignedLocationReport.Builder builder = SignedLocationReport.newBuilder();
//            JsonFormat.parser().merge(l, builder);
//
//            LocationProof locationProof = LocationProof.newBuilder()
//                    .setWitnessId(3)
//                    .setProverId(1)
//                    .setEpoch(3)
//                    .setPosition(Position.newBuilder().setX(72).setY(32).build())
//                    .build();
//
//            SignedLocationProof signedLocationProof = SignedLocationProof.newBuilder().
//                    setLocationProof(locationProof)
//                    .setSignature(ByteString.copyFrom(CryptographicOperations.sign(locationProof.toByteArray(), FileUtils.getUserPrivateKey(3))))
//                    .build();
//
//            LocationReport.Builder locationReportBuilder = builder.getLocationReport().toBuilder();
//            locationReportBuilder.addLocationProof(signedLocationProof);
//            builder.setLocationReport(locationReportBuilder.build());
//
//            builder.setUserSignature(ByteString.copyFrom(CryptographicOperations.sign(builder.getLocationReport().toByteArray(), FileUtils.getUserPrivateKey(1))));
//
//            SignedLocationReport signedLocationReport = builder.build();
//
//            locationBL.handleSubmitLocationReport(signedLocationReport);
//
//            fail();
//        } catch (Exception e) {
//            assertEquals("Invalid location proof", e.getMessage());
//        }
//    }
//
//    @Test
//    public void testSubmitProofWithIncorrectPosition() {
//
//        try {
//            LocationBL locationBL = new LocationBL(1, 1);
//
//            Path path = Paths.get("../Server/src/test/resources/incorrect_proof_location.txt");
//            String l = Files.readString(path).split("\n")[0];
//
//            // {      "locationProof": {        "witnessId": 3,        "proverId": 1,        "epoch": "2",        "position": {          "x": "2",          "y": "32"        }      },      "signature": "hThW/wZHl0KhUzLh9dSmv1kE3hMb0Hys8N6XLwFrJjJaN197gIwBxvk65Fezvi5waoELWfvDiUkEjpKQlhffJYEN8noOx/UK/v/e6BNKPSuYq8bzb09Vt+Ugb99EnpbqGfiFivTVYX10HyKIUwoKTMTV6LvizEH/yH2sr/yTDnf0+i95VX8yqplDapaH4rI2rLubX4eWKeCW4xiG7aVs88KH3Fbac1DX1MhgkVSInu8FUG5lSYsrk8urIyl7sxC7Z+SBDMl2kTNzbjoJ6UNZrayRi74NzrACbFPzirkeqS6zKmLOgyalZrsi9kEI0/D3IgL0mv2lACb188bLJnJnOA=="    },
//
//            SignedLocationReport.Builder builder = SignedLocationReport.newBuilder();
//            JsonFormat.parser().merge(l, builder);
//
//            LocationProof locationProof = LocationProof.newBuilder()
//                    .setWitnessId(3)
//                    .setProverId(1)
//                    .setEpoch(2)
//                    .setPosition(Position.newBuilder().setX(2).setY(32).build())
//                    .build();
//
//            SignedLocationProof signedLocationProof = SignedLocationProof.newBuilder().
//                    setLocationProof(locationProof)
//                    .setSignature(ByteString.copyFrom(CryptographicOperations.sign(locationProof.toByteArray(), FileUtils.getUserPrivateKey(3))))
//                    .build();
//
//            LocationReport.Builder locationReportBuilder = builder.getLocationReport().toBuilder();
//            locationReportBuilder.addLocationProof(signedLocationProof);
//            builder.setLocationReport(locationReportBuilder.build());
//
//            builder.setUserSignature(ByteString.copyFrom(CryptographicOperations.sign(builder.getLocationReport().toByteArray(), FileUtils.getUserPrivateKey(1))));
//
//            SignedLocationReport signedLocationReport = builder.build();
//
//            locationBL.handleSubmitLocationReport(signedLocationReport);
//
//            fail();
//        } catch (Exception e) {
//            assertEquals("Invalid location proof", e.getMessage());
//        }
//    }
}
