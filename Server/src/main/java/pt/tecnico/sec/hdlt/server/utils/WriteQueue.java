package pt.tecnico.sec.hdlt.server.utils;

import com.google.protobuf.InvalidProtocolBufferException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.LinkedBlockingQueue;

public abstract class WriteQueue<T> implements Runnable {

    protected final LinkedBlockingQueue<T> writes;
    private final Path file;
    private volatile boolean running;
    private final Thread thread;

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
                Files.writeString(this.file, stringToWrite(),
                        StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            } catch (IOException e) {
                System.out.println("Error writing to file");
            } catch (InterruptedException e) {
                this.running = false;
            }
        }
    }

    protected abstract String stringToWrite() throws InterruptedException, InvalidProtocolBufferException;

    public void terminate() throws InterruptedException {
        while (!this.writes.isEmpty()) {}

        this.thread.interrupt();
        this.thread.join();
    }
}
