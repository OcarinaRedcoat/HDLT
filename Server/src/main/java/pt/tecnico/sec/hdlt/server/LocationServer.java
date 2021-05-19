package pt.tecnico.sec.hdlt.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.sec.hdlt.server.bll.LocationBL;
import pt.tecnico.sec.hdlt.server.service.LocationServerService;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class LocationServer {

    private static final Logger logger = Logger.getLogger(App.class.getName());
    private Server server;
    private LocationBL locationBL;
    private int id;

    public LocationServer(int id, LocationBL locationBL) {
        this.id = id;
        this.locationBL = locationBL;
        start();
    }

    private void start() {
        try{
            /* The port on which the server should run */
            int port = 50050 + (id - 1);
            this.server = ServerBuilder.forPort(port)
                        .addService(new LocationServerService(locationBL))
                        .build()
                        .start();
            logger.info("Server started, listening on " + port);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                    System.err.println("*** shutting down gRPC server since JVM is shutting down");
                    LocationServer.this.stop();
                }
            });
        } catch (IOException e) {
            System.out.println("Could not start server.");
            System.exit(1);
        }
    }

    public void stop() {
        this.locationBL.closeServerChannel();
        if (this.server != null) {
            try{
                this.server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                System.err.println("*** server shut down");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        terminateWrite();
    }

    private void terminateWrite(){
        try {
            locationBL.terminateMessageWriteQueue();
            locationBL.terminateNonceWriteQueue();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
