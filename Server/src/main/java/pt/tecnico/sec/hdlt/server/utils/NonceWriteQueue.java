package pt.tecnico.sec.hdlt.server.utils;

import java.nio.file.Path;

public class NonceWriteQueue extends WriteQueue<String> {

    public NonceWriteQueue(Path file) {
        super(file);
    }

    @Override
    protected String stringToWrite() throws InterruptedException {
        return this.writes.take();
    }
}
