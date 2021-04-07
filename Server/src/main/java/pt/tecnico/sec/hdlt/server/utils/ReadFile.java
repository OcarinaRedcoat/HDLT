package pt.tecnico.sec.hdlt.server.utils;

import com.google.protobuf.util.JsonFormat;
import pt.tecnico.sec.hdlt.communication.LocationReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ReadFile {

    public static ConcurrentHashMap<String, LocationReport> createReportsMap(Path path) {
        ConcurrentHashMap<String, LocationReport> locationReports = new ConcurrentHashMap<>();

        for (LocationReport report : readLocationReports(path)) {
            locationReports.put(report.getLocationInformation().getUserId(), report);
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
