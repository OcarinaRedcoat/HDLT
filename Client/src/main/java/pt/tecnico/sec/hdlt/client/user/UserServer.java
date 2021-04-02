package pt.tecnico.sec.hdlt.client.user;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.communication.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class UserServer {

    private static UserServer INSTANCE = null;
    private static final Logger logger = Logger.getLogger(UserServer.class.getName());

    private Server server;

    public static UserServer getInstance() {
        if (INSTANCE == null)
            INSTANCE = new UserServer();

        return INSTANCE;
    }

    public UserServer() {
        server = null;
    }

    public void start() throws IOException {
        /* The port on which the server should run */
        server = ServerBuilder.forPort(Client.getInstance().getUser().getPort())
                .addService(new UserImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + Client.getInstance().getUser().getPort());
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    UserServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    public void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    public void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }


}