package pt.tecnico.sec.hdlt.client.user;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.communication.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class UserServer {

    private int port;

    private Server server;

    private User myUser;

    private static final Logger logger = Logger.getLogger(UserServer.class.getName());

    public UserServer(User myUser) {
        this.port = myUser.getPort();
        this.myUser = myUser;
    }

    public void start() throws IOException {
        /* The port on which the server should run */
        int port = this.port;
        server = ServerBuilder.forPort(port)
                .addService(new ProofImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
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

    static class ProofImpl extends LocationServerGrpc.LocationServerImplBase{
        @Override
        public void requestLocationProof(LocationProofRequest req, StreamObserver<LocationProofResponse> responseObserver){
           //Ir buscar location do user ......... but how
            int requesterId = Integer.parseInt(req.getUserId());
            int currentEpoch = req.getEpoch();
            int requesterX = req.getRequesterX();
            int requesterY = req.getRequesterY();

        }
    }

}