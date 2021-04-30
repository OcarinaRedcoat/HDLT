package pt.tecnico.sec.hdlt.server.utils;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.util.JsonFormat;

import java.nio.file.Path;

public class MessageWriteQueue extends WriteQueue<MessageOrBuilder> {

    public MessageWriteQueue(Path file) {
        super(file);
    }

    @Override
    protected String stringToWrite() throws InterruptedException, InvalidProtocolBufferException {
        return JsonFormat.printer().print(this.writes.take()).replace("\n", "") + "\n";
    }
}
