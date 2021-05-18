package pt.tecnico.sec.hdlt.client.services;

import io.grpc.ServerBuilder;
import pt.tecnico.sec.hdlt.entities.Client;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class Server {

    private static final Logger logger = Logger.getLogger(Server.class.getName());

    private io.grpc.Server server;

    public Server(Client client) {
        start(client);
    }

    private void start(Client client) {
        try{
            /* The port on which the server should run */
            server = ServerBuilder.forPort(client.getUser().getPort())
                    .addService(new Impl(client))
                    .build()
                    .start();
            logger.info("Server started, listening on " + client.getUser().getPort());
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                    System.err.println("*** shutting down gRPC server since JVM is shutting down");
                    Server.this.stop();
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
                System.err.println("*** server shut down");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}