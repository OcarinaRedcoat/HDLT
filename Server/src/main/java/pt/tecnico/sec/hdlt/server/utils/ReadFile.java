package pt.tecnico.sec.hdlt.server.utils;

import com.google.protobuf.util.JsonFormat;
import pt.tecnico.sec.hdlt.communication.LocationReport;
import pt.tecnico.sec.hdlt.server.entities.LocationReportKey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ReadFile {

    public static ConcurrentHashMap<LocationReportKey, LocationReport> createReportsMap(Path path) {
        ConcurrentHashMap<LocationReportKey, LocationReport> locationReports = new ConcurrentHashMap<>();

        for (LocationReport report : readLocationReports(path)) {
            locationReports.put(new LocationReportKey(report.getLocationInformation().getUserId(), report.getLocationInformation().getEpoch()), report);
        }

        return locationReports;
    }

    private static List<LocationReport> readLocationReports(Path path) {
        List<LocationReport> locationReports = new ArrayList<>();
        LocationReport.Builder builder;

        try {
            for (String l : Files.readString(path).split("\n")) {
                builder = LocationReport.newBuilder();
                JsonFormat.parser().merge(l, builder);
                locationReports.add(builder.build());
            }
        } catch (IOException e) {
            System.err.println("Unable to read from file: " + e.getMessage());
        }

        return locationReports;
    }
}
