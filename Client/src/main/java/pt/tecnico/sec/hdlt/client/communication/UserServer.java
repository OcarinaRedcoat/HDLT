package pt.tecnico.sec.hdlt.client.communication;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import pt.tecnico.sec.hdlt.entities.Client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class UserServer {

    private static final Logger logger = Logger.getLogger(UserServer.class.getName());

    private Server server;

    public UserServer(Client client) {
        start(client);
        server = null;
    }

    public void start(Client client) {
        try{
            /* The port on which the server should run */
            server = ServerBuilder.forPort(client.getUser().getPort())
                    .addService(new UserImpl(client))
                    .build()
                    .start();
            logger.info("Server started, listening on " + client.getUser().getPort());
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                    System.err.println("*** shutting down gRPC server since JVM is shutting down");
                    UserServer.this.stop();
                    System.err.println("*** server shut down");
                }
            });
        } catch (IOException e) {
            System.out.println("Could not start server.");
            System.exit(1);
        }
    }

    public void stop() {
        if (server != null) {
            try{
                server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
                System.out.println("Server shut down.");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}