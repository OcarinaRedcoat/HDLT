package pt.tecnico.sec.hdlt.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.sec.hdlt.server.bll.LocationBL;
import pt.tecnico.sec.hdlt.server.service.LocationServerService;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LocationServer {

    private static final Logger logger = Logger.getLogger(LocationServer.class.getName());
    private Server server;

    private void start(LocationBL locationBL) throws IOException {
        /* The port on which the server should run */
        int port = 50051;
        this.server = ServerBuilder.forPort(port)
                .addService(new LocationServerService(locationBL)).build().start();

        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    LocationServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (this.server != null) {
            this.server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (this.server != null) {
            this.server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        Path filePath = Paths.get("Server" + 1 + ".txt");
        LocationBL locationBL = new LocationBL(filePath);

        final LocationServer locationServer = new LocationServer();
        locationServer.start(locationBL);

        locationBL.terminateWriteQueue();
    }
}