package pt.tecnico.sec.hdlt.server.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LocationWriter implements Runnable {

    private FileWriter fileWriter;

    public LocationWriter(int serverId) throws IOException {
        this.fileWriter = new FileWriter(new File("Server" + serverId + ".txt").getAbsolutePath(), true);
    }

    @Override
    public void run() {

    }
}
