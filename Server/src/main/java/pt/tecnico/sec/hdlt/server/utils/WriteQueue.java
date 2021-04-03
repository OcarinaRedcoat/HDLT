package pt.tecnico.sec.hdlt.server.utils;

import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.LinkedBlockingQueue;

public class WriteQueue<T extends MessageOrBuilder> implements Runnable {

    private LinkedBlockingQueue<T> writes;
    private Path file;
    private volatile boolean running;
    private Thread thread;

    public WriteQueue(Path file) {
        this.writes = new LinkedBlockingQueue<>();
        this.file = file;
        this.running = true;

        this.thread = new Thread(this);
        this.thread.start();
    }

    public void write(T message) throws InterruptedException {
        this.writes.put(message);
    }

    @Override
    public void run() {
        while (this.running) {
            try {
                Files.writeString(this.file, JsonFormat.printer().print(this.writes.take()).replace("\n", "") + "\n",
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.out.println("Error writing to file");
            } catch (InterruptedException e) {
                this.running = false;
            }
        }
    }

    public void terminate() throws InterruptedException {
        while (!this.writes.isEmpty()) {}

        this.thread.interrupt();
        this.thread.join();
    }
}
