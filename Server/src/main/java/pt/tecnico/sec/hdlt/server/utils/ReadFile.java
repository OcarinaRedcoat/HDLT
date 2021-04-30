package pt.tecnico.sec.hdlt.server.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import pt.tecnico.sec.hdlt.communication.SignedLocationReport;
import pt.tecnico.sec.hdlt.server.entities.LocationReportKey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ReadFile {

    public static ConcurrentHashMap<LocationReportKey, SignedLocationReport> createReportsMap(Path path) {
        ConcurrentHashMap<LocationReportKey, SignedLocationReport> locationReports = new ConcurrentHashMap<>();

        for (SignedLocationReport report : readLocationReports(path)) {
            locationReports.put(new LocationReportKey(
                    report.getLocationReport().getLocationInformation().getUserId(),
                    report.getLocationReport().getLocationInformation().getEpoch()), report);
        }

        return locationReports;
    }

    private static List<SignedLocationReport> readLocationReports(Path path) {
        List<SignedLocationReport> locationReports = new ArrayList<>();
        SignedLocationReport.Builder builder;

        try {
            for (String l : Files.readString(path).split("\n")) {
                builder = SignedLocationReport.newBuilder();
                try {
                    JsonFormat.parser().merge(l, builder);
                    locationReports.add(builder.build());
                } catch (InvalidProtocolBufferException ignored) { }
            }
        } catch (IOException e) {
            System.err.println("Unable to read from file " + path.getFileName());
        }

        return locationReports;
    }

    public static Set<String> createNonceSet(Path nonceFilePath) {
        Set<String> nonceSet = ConcurrentHashMap.newKeySet();

        try {
            nonceSet.addAll(Arrays.asList(Files.readString(nonceFilePath).split("\n")));
        } catch (IOException e) {
            System.err.println("Unable to read from file " + nonceFilePath.getFileName());
        }

        return nonceSet;
    }
}
