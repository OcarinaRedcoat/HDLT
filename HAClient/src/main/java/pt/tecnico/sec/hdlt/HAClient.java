package pt.tecnico.sec.hdlt;

import com.google.protobuf.ByteString;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import pt.tecnico.sec.hdlt.communication.*;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;

import static pt.tecnico.sec.hdlt.FileUtils.getServerPublicKey;
import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.*;
import static pt.tecnico.sec.hdlt.crypto.CryptographicOperations.symmetricDecrypt;
public class HAClient {

    private static HAClient INSTANCE = null;
    private static HA ha = null;

    private LocationServerGrpc.LocationServerBlockingStub serverStub;
    private ManagedChannel serverChannel;

    private HAClient(String host, int port) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        try {
            createServerChannel(host, port);
            HA.getInstance().initializeHA();
        }catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e){
            System.out.println(e.getMessage());
            System.exit(-1);
        }
    }


    private void createServerChannel(String host, int port){
        String target = host + ":" + String.valueOf(port);
        serverChannel = ManagedChannelBuilder.forTarget(target)
                .usePlaintext()
                .build();
        serverStub = LocationServerGrpc.newBlockingStub(serverChannel);
    }

    public void serverShutdown(){
        serverChannel.shutdownNow();
    }

    public static HAClient getInstance() throws NoSuchAlgorithmException, IOException, InvalidKeySpecException {
        if (INSTANCE == null)
            INSTANCE = new HAClient("localhost", 50051);
        return INSTANCE;
    }


    /* Params: userId, ep .....
     * Specification: returns the position of the userId at the epoch ep, The HA can
     *   obtain the location information of any user
     * TODO fazer um novo rpc proto pois tenho que dizer que sou o HA e quem quero
     * */
    public static LocationReport obtainLocationReport(int userId, Long epoch, LocationServerGrpc.LocationServerBlockingStub serverStub)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, NoSuchPaddingException,
            BadPaddingException, IllegalBlockSizeException, IOException, InvalidKeySpecException {

        HALocationQuery locationQuery = HALocationQuery
                .newBuilder()
                .setUserId(userId)
                .setEpoch(epoch)
                .setHaId(HA.getInstance().getHAId())
                .build();
        //TODO make a signature for HA
        byte[] signature = sign(locationQuery.toByteArray(), HA.getInstance().getPrivateKey());

        HASignedLocationQuery signedLocationQuery = HASignedLocationQuery
                .newBuilder()
                .setLocationQuery(locationQuery)
                .setSignature(ByteString.copyFrom(signature))
                .build();

        SecretKey key = generateSecretKey();

        //TODO IV
        byte[] encryptedMessage = symmetricEncrypt(signedLocationQuery.toByteArray(), key);
        byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(1));

        //TODO: IV
        HAObtainLocationReportRequest request = HAObtainLocationReportRequest
                .newBuilder()
                .setKey(ByteString.copyFrom(encryptedKey))
                .setEncryptedSignedLocationQuery(ByteString.copyFrom(encryptedMessage))
                .build();

        HAObtainLocationReportResponse response = serverStub.hAObtainLocationReport(request);

        byte[] decryptedMessage = symmetricDecrypt(response.getEncryptedSignedLocationReport().toByteArray(), key);

        SignedLocationReport signedLocationReport = SignedLocationReport.parseFrom(decryptedMessage);

        LocationReport report = signedLocationReport.getLocationReport();

        if(!verifySignature(getServerPublicKey(1), report.toByteArray(),
                signedLocationReport.getSignedLocationReport().toByteArray())){
            //TODO: exception
            throw new InvalidKeyException();
        }

        return report;
    }
    /* Params: pos, ep .....
     * Specification: returns a list of users that were at position pos at epoch ep
     * TODO criar metodo grpc que vais buscar user por posicoes, duvida se podemos ter
     *  mais que um utilizador por posicao
     */
    public void obtainUsersAtLocation(){}

}

