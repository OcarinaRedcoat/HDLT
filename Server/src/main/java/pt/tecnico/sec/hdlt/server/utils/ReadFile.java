package pt.tecnico.sec.hdlt.server.utils;

import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;
import pt.tecnico.sec.hdlt.communication.LocationInformation;
import pt.tecnico.sec.hdlt.communication.LocationProof;
import pt.tecnico.sec.hdlt.communication.LocationReport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ReadFile<T extends MessageOrBuilder> {

    public static List<LocationReport> readLocationReports(Path path) {
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
