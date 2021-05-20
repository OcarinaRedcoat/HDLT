package pt.tecnico.sec.hdlt.server.bll;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.sun.jdi.InternalException;
import io.grpc.Context;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import pt.tecnico.sec.hdlt.server.entities.BroadcastVars;
import pt.tecnico.sec.hdlt.utils.FileUtils;
import pt.tecnico.sec.hdlt.utils.GeneralUtils;
import pt.tecnico.sec.hdlt.communication.*;
import pt.tecnico.sec.hdlt.utils.CryptographicUtils;
import pt.tecnico.sec.hdlt.server.entities.LocationReportKey;
import pt.tecnico.sec.hdlt.server.utils.MessageWriteQueue;
import pt.tecnico.sec.hdlt.server.utils.NonceWriteQueue;
import pt.tecnico.sec.hdlt.server.utils.ReadFile;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import static pt.tecnico.sec.hdlt.utils.CryptographicUtils.*;
import static pt.tecnico.sec.hdlt.utils.FileUtils.getServerPublicKey;
import static pt.tecnico.sec.hdlt.utils.GeneralUtils.*;
import static pt.tecnico.sec.hdlt.utils.ProtoUtils.*;

public class LocationBL {

    private final MessageWriteQueue messageWriteQueue;
    private final NonceWriteQueue nonceWriteQueue;
    private final ConcurrentHashMap<LocationReportKey, SignedLocationReport> locationReports;
    private final ConcurrentHashMap<SignedLocationReport, BroadcastVars> broadcast;
    private final Set<String> nonceSet;
    private final int numberByzantineUsers;
    private PrivateKey privateKey;

    private int serverId;
    private ArrayList<LocationServerGrpc.LocationServerStub> serverStubs;
    private ArrayList<ManagedChannel> serverChannels;

    public LocationBL(int serverId, String serverPwd) {
        Path messageFilePath = Paths.get("../Server/src/main/resources/server_" + serverId + ".txt");
        Path nonceFilePath = Paths.get("../Server/src/main/resources/server_" + serverId + "_nonce.txt");

        this.serverId = serverId;
        this.messageWriteQueue = new MessageWriteQueue(messageFilePath);
        this.nonceWriteQueue = new NonceWriteQueue(nonceFilePath);
        this.locationReports = ReadFile.createReportsMap(messageFilePath);
        this.broadcast = new ConcurrentHashMap<>();
        this.nonceSet = ReadFile.createNonceSet(nonceFilePath);

        createServerStubs();

        try {
            this.privateKey = CryptographicUtils.getServerPrivateKey(serverId, serverPwd);
        } catch (Exception e) {
            System.err.println("There was a problem reading the server private key. Make sure the keyStore exists and is correct.");
            System.exit(1);
        }

        this.numberByzantineUsers = GeneralUtils.F;
    }

    private void createServerStubs(){
        serverStubs = new ArrayList<>();
        serverChannels = new ArrayList<>();
        for (int i = 0; i < N_SERVERS; i++) {
            String target = SERVER_HOST + ":" + (SERVER_START_PORT + i);
            ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                    .usePlaintext()
                    .build();
            serverChannels.add(channel);
            LocationServerGrpc.LocationServerStub stub = LocationServerGrpc.newStub(channel);
            serverStubs.add(stub);
        }
    }

    public void closeServerChannel(){
        for (ManagedChannel channel : serverChannels) {
            try {
                channel.shutdown().awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        serverChannels = new ArrayList<>();
        serverStubs = new ArrayList<>();
    }

    public SubmitLocationReportResponse submitLocationReport(SubmitLocationReportRequest request) throws Exception {
        // Authenticate request
        byte[] key = decryptKey(request.getKey().toByteArray(), this.privateKey);
        byte[] authSignedReportBytes = decryptRequest(request.getEncryptedAuthenticatedSignedLocationReportWrite().toByteArray(), key, request.getIv().toByteArray());
        AuthenticatedSignedLocationReportWrite authSignedReport = AuthenticatedSignedLocationReportWrite.parseFrom(authSignedReportBytes);
        SignedLocationReportWrite signedReportWrite = authSignedReport.getSignedLocationReportWrite();
        SignedLocationReport signedReport = signedReportWrite.getSignedLocationReport();

        if(!isValidPoW(signedReportWrite)){
            return submitLocationReportResponse(signedReportWrite.getRid(),"Invalid pow", key);
        }

        if (this.nonceSet.contains(signedReportWrite.getNonce())) {
            return submitLocationReportResponse(signedReportWrite.getRid(),"Invalid nonce", key);
        }

        this.nonceSet.add(signedReportWrite.getNonce());
        this.nonceWriteQueue.write(signedReportWrite.getNonce());

        boolean verifySignature = signedReportWrite.getIsHa() ?
                verifyHaSignature(authSignedReport.getSignedLocationReportWrite().toByteArray(), authSignedReport.getSignature().toByteArray()) :
                verifySignature(signedReport.getLocationReport().getLocationInformation().getUserId(),
                        authSignedReport.getSignedLocationReportWrite().toByteArray(), authSignedReport.getSignature().toByteArray());

        if (!verifySignature) {
            throw new InvalidParameterException("Invalid location query signature");
        }

        return handleSubmitLocationReport(signedReportWrite, key);
    }

    public SubmitLocationReportResponse handleSubmitLocationReport(SignedLocationReportWrite signedReportWrite, byte[] key) throws Exception {
        SignedLocationReport signedReport = signedReportWrite.getSignedLocationReport();
        LocationReport report = signedReport.getLocationReport();
        LocationInformation information = report.getLocationInformation();

        LocationReportKey rKey = new LocationReportKey(information.getUserId(), information.getEpoch());
        if (this.locationReports.containsKey(rKey)) {
            return submitLocationReportResponse(signedReportWrite.getRid(), "Repeated location report", key);
        }

        boolean validReport = true;
        String message = "OK";
        if (!validReport(signedReport, this.numberByzantineUsers)) {
            validReport = false;
            message = "Invalid location report";
        }

        signedReport = signedReport.toBuilder().setValid(validReport).build();

        BroadcastVars broadcastVars = new BroadcastVars();
        broadcastVars.setSentEcho(true);
        BroadcastVars aux = broadcast.putIfAbsent(signedReport, broadcastVars);
        if(aux != null){
            if(aux.setSentEcho(true)){
                return submitLocationReportResponse(signedReportWrite.getRid(), "Already in echo phase", key);
            }
            broadcastVars = aux;
        }

        submitEcho(signedReport);
        broadcastVars.getBlocker().await();
        return submitLocationReportResponse(signedReportWrite.getRid(), message, key);
    }

    private void deliver(SignedLocationReport signedReport, LocationReportKey rKey) throws Exception {
        this.locationReports.put(rKey, signedReport);
        this.messageWriteQueue.write(signedReport);
        System.out.println("Store report successfully");
    }

    public void submitEcho(SignedLocationReport signedLocationReport) {
        Context context = Context.current().fork();
        context.run(() -> {
            try {
                Echo echo = buildEcho(serverId, signedLocationReport);

                byte[] signature = sign(echo.toByteArray(), this.privateKey);
                ServerSignedEcho serverSignedEcho = ServerSignedEcho.newBuilder()
                        .setEcho(echo)
                        .setSignature(ByteString.copyFrom(signature))
                        .build();

                SecretKey key = generateSecretKey();
                IvParameterSpec iv = generateIv();
                byte[] encryptedMessage = symmetricEncrypt(serverSignedEcho.toByteArray(), key, iv);


                EchoRequest.Builder echoRequestBuilder = EchoRequest.newBuilder()
                        .setIv(ByteString.copyFrom(iv.getIV()))
                        .setEncryptedServerSignedEcho(ByteString.copyFrom(encryptedMessage));

                StreamObserver<EchoResponse> observer = new StreamObserver<>() {
                    @Override
                    public void onNext(EchoResponse response) {

                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {

                    }
                };

                EchoRequest request;
                for (int i = 0; i < serverStubs.size(); i++) {
                    byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(i + 1));
                    request = echoRequestBuilder.setEncryptedKey(ByteString.copyFrom(encryptedKey)).build();
                    serverStubs.get(i).echo(request, observer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void submitReady(SignedLocationReport signedLocationReport) {
        Context context = Context.current().fork();
        context.run(() -> {
            try {
                Ready ready = buildReady(serverId, signedLocationReport);

                byte[] signature = sign(ready.toByteArray(), this.privateKey);
                ServerSignedReady serverSignedReady = ServerSignedReady.newBuilder()
                        .setReady(ready)
                        .setSignature(ByteString.copyFrom(signature))
                        .build();

                SecretKey key = generateSecretKey();
                IvParameterSpec iv = generateIv();
                byte[] encryptedMessage = symmetricEncrypt(serverSignedReady.toByteArray(), key, iv);

                ReadyRequest.Builder readyRequestBuilder = ReadyRequest.newBuilder()
                        .setIv(ByteString.copyFrom(iv.getIV()))
                        .setEncryptedServerSignedReady(ByteString.copyFrom(encryptedMessage));

                StreamObserver<ReadyResponse> observer = new StreamObserver<>() {
                    @Override
                    public void onNext(ReadyResponse response) {

                    }

                    @Override
                    public void onError(Throwable t) {

                    }

                    @Override
                    public void onCompleted() {
                    }
                };

                ReadyRequest request;
                for (int i = 0; i < serverStubs.size(); i++) {
                    byte[] encryptedKey = asymmetricEncrypt(key.getEncoded(), getServerPublicKey(i + 1));
                    request = readyRequestBuilder.setEncryptedKey(ByteString.copyFrom(encryptedKey)).build();
                    serverStubs.get(i).ready(request, observer);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private SubmitLocationReportResponse submitLocationReportResponse(int rid, String message, byte[] key) throws Exception {
        Ack ack = Ack.newBuilder().setRid(rid).setMessage(message).build();

        SignedAck signedAck  = SignedAck.newBuilder()
                .setAck(ack)
                .setSignature(ByteString.copyFrom(CryptographicUtils.sign(ack.toByteArray(), this.privateKey)))
                .build();

        IvParameterSpec iv = CryptographicUtils.generateIv();
        return SubmitLocationReportResponse.newBuilder()
                .setEncryptedSignedAck(ByteString.copyFrom(encryptResponse(signedAck.toByteArray(), key, iv)))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .build();
    }

    public ObtainLocationReportResponse obtainLocationReport(ObtainLocationReportRequest request) throws Exception {
        byte[] secretKey = decryptKey(request.getKey().toByteArray(), this.privateKey);

        byte[] queryBytes = decryptRequest(
                request.getEncryptedSignedLocationQuery().toByteArray(),
                secretKey,
                request.getIv().toByteArray());

        SignedLocationQuery sLocationQuery = SignedLocationQuery.parseFrom(queryBytes);
        LocationQuery locationQuery = sLocationQuery.getLocationQuery();

        if(!isValidPoW(locationQuery)){
            throw new InvalidParameterException("Invalid pow");
        }

        if (this.nonceSet.contains(locationQuery.getNonce())) {
            throw new InvalidParameterException("Invalid nonce");
        }

        this.nonceSet.add(locationQuery.getNonce());
        this.nonceWriteQueue.write(locationQuery.getNonce());

        boolean verifySignature = locationQuery.getIsHA() ?
                verifyHaSignature(locationQuery.toByteArray(), sLocationQuery.getSignature().toByteArray()) :
                verifySignature(locationQuery.getUserId(), locationQuery.toByteArray(), sLocationQuery.getSignature().toByteArray());

        if (!verifySignature) {
            throw new InvalidParameterException("Invalid location query signature");
        }

        LocationReportKey key = new LocationReportKey(locationQuery.getUserId(), locationQuery.getEpoch());

        SignedLocationReport report = this.locationReports.get(key);

        SignedLocationReportRid signedLocationReportRid = SignedLocationReportRid.newBuilder()
                .setSignedLocationReport(report)
                .setRid(locationQuery.getRid())
                .build();

        ServerSignedSignedLocationReportRid signedSignedLocationReportRid = ServerSignedSignedLocationReportRid.newBuilder()
                .setSignedLocationReportRid(signedLocationReportRid)
                .setServerSignature(ByteString.copyFrom(CryptographicUtils.sign(signedLocationReportRid.toByteArray(), this.privateKey)))
                .build();

        IvParameterSpec iv = CryptographicUtils.generateIv();

        return ObtainLocationReportResponse.newBuilder()
                .setEncryptedServerSignedSignedLocationReportRid(ByteString.copyFrom(encryptResponse(signedSignedLocationReportRid.toByteArray(), secretKey, iv)))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .build();
    }

    public ObtainUsersAtLocationResponse obtainUsersAtLocation(ObtainUsersAtLocationRequest request) throws Exception {
        byte[] secretKey = decryptKey(request.getKey().toByteArray(), this.privateKey);

        byte[] queryBytes = decryptRequest(
                request.getEncryptedSignedUsersAtLocationQuery().toByteArray(),
                secretKey,
                request.getIv().toByteArray());

        SignedUsersAtLocationQuery sUsersAtLocationQuery = SignedUsersAtLocationQuery.parseFrom(queryBytes);
        UsersAtLocationQuery usersAtLocationQuery = sUsersAtLocationQuery.getUsersAtLocationQuery();

        if(!isValidPoW(usersAtLocationQuery)){
            throw new InvalidParameterException("Invalid pow");
        }

        if (this.nonceSet.contains(usersAtLocationQuery.getNonce())) {
            throw new InvalidParameterException("Invalid nonce");
        }

        this.nonceSet.add(usersAtLocationQuery.getNonce());
        this.nonceWriteQueue.write(usersAtLocationQuery.getNonce());

        if (!verifyHaSignature(usersAtLocationQuery.toByteArray(), sUsersAtLocationQuery.getSignature().toByteArray())) {
            throw new InvalidParameterException("Invalid users at location query signature");
        }

        SignedLocationReportList.Builder builder = SignedLocationReportList.newBuilder();

        for (SignedLocationReport report : this.locationReports.values()) {
            LocationInformation information = report.getLocationReport().getLocationInformation();
            if (information.getEpoch() == usersAtLocationQuery.getEpoch() &&
                    information.getPosition().getX() == usersAtLocationQuery.getPos().getX() &&
                    information.getPosition().getY() == usersAtLocationQuery.getPos().getY()) {

                builder.addSignedLocationReportList(report);
            }
        }

        SignedLocationReportList signedLocationReportList = builder.setRid(usersAtLocationQuery.getRid()).build();

        ServerSignedSignedLocationReportList serverSignedSignedLocationReportList = ServerSignedSignedLocationReportList
                .newBuilder()
                .setSignedLocationReportList(signedLocationReportList)
                .setServerSignature(ByteString.copyFrom(CryptographicUtils.sign(signedLocationReportList.toByteArray(), this.privateKey)))
                .build();

        IvParameterSpec iv = CryptographicUtils.generateIv();

        return ObtainUsersAtLocationResponse.newBuilder()
                .setEncryptedSignedLocationReportList(
                        ByteString.copyFrom(
                                encryptResponse(serverSignedSignedLocationReportList.toByteArray(), secretKey, iv)))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .build();
    }

    public RequestMyProofsResponse requestMyProofs(RequestMyProofsRequest request) throws Exception {
        byte[] secretKey = decryptKey(request.getKey().toByteArray(), this.privateKey);

        byte[] queryBytes = decryptRequest(
                request.getEncryptedSignedProofsQuery().toByteArray(),
                secretKey,
                request.getIv().toByteArray());

        SignedProofsQuery signedProofsQuery = SignedProofsQuery.parseFrom(queryBytes);
        ProofsQuery proofsQuery = signedProofsQuery.getProofsQuery();

        if(!isValidPoW(proofsQuery)){
            throw new InvalidParameterException("Invalid pow");
        }

        if (this.nonceSet.contains(proofsQuery.getNonce())) {
            throw new InvalidParameterException("Invalid nonce");
        }

        this.nonceSet.add(proofsQuery.getNonce());
        this.nonceWriteQueue.write(proofsQuery.getNonce());

        if (!verifySignature(proofsQuery.getUserId(), proofsQuery.toByteArray(), signedProofsQuery.getSignature().toByteArray())) {
            throw new InvalidParameterException("Invalid signature");
        }

        Proofs.Builder builder = Proofs.newBuilder();

        for (SignedLocationReport report : this.locationReports.values()) {
            if (!proofsQuery.getEpochsList().contains(report.getLocationReport().getLocationInformation().getEpoch())) {
                continue;
            }

            for (SignedLocationProof sProof : report.getLocationReport().getLocationProofList()) {
                if (sProof.getLocationProof().getWitnessId() == proofsQuery.getUserId()) {
                    builder.addLocationProof(sProof);
                }
            }
        }

        Proofs proofs = builder.setRid(proofsQuery.getRid()).build();

        ServerSignedProofs serverSignedProofs = ServerSignedProofs.newBuilder()
                .setProofs(proofs)
                .setServerSignature(ByteString.copyFrom(CryptographicUtils.sign(proofs.toByteArray(), this.privateKey)))
                .build();

        IvParameterSpec iv = CryptographicUtils.generateIv();

        return RequestMyProofsResponse.newBuilder()
                .setEncryptedServerSignedProofs(ByteString.copyFrom(encryptResponse(serverSignedProofs.toByteArray(), secretKey, iv)))
                .setIv(ByteString.copyFrom(iv.getIV()))
                .build();
    }

    public EchoResponse echo(EchoRequest request) throws Exception {
        byte[] key = decryptKey(request.getEncryptedKey().toByteArray(), this.privateKey);
        byte[] authSignedEchoBytes = decryptRequest(request.getEncryptedServerSignedEcho().toByteArray(), key, request.getIv().toByteArray());
        ServerSignedEcho serverSignedEcho = ServerSignedEcho.parseFrom(authSignedEchoBytes);
        Echo echo = serverSignedEcho.getEcho();
        SignedLocationReport signedReport = echo.getSignedLocationReport();

        if(!isValidPoW(echo)){
            return EchoResponse.newBuilder().build();
        }

        if (this.nonceSet.contains(echo.getNonce())) {
            return EchoResponse.newBuilder().build();
        }

        this.nonceSet.add(echo.getNonce());
        this.nonceWriteQueue.write(echo.getNonce());

        if(!verifyServerSignature(echo.getServerId(), serverSignedEcho.getEcho().toByteArray(),
                serverSignedEcho.getSignature().toByteArray())){
            return EchoResponse.newBuilder().build();
        }

        if(!verifySignature(signedReport.getLocationReport().getLocationInformation().getUserId(),
                signedReport.getLocationReport().toByteArray(),
                signedReport.getUserSignature().toByteArray())){
            return EchoResponse.newBuilder().build();
        }

        BroadcastVars broadcastVars = new BroadcastVars();
        broadcastVars.getEchos().add(serverSignedEcho);
        BroadcastVars aux = broadcast.putIfAbsent(signedReport, broadcastVars);
        if(aux != null){
            aux.addEcho(serverSignedEcho);
            broadcastVars = aux;
        }

        if(broadcastVars.getEchos().size() > (N_SERVERS + F)/2 && !broadcastVars.getSentReady()){
            broadcastVars.setSentReady(true);
            submitReady(signedReport);
        }

        return EchoResponse.newBuilder().build();
    }

    public ReadyResponse ready(ReadyRequest request) throws Exception {
        byte[] key = decryptKey(request.getEncryptedKey().toByteArray(), this.privateKey);
        byte[] authSignedEchoBytes = decryptRequest(request.getEncryptedServerSignedReady().toByteArray(), key, request.getIv().toByteArray());
        ServerSignedReady serverSignedReady = ServerSignedReady.parseFrom(authSignedEchoBytes);
        Ready ready = serverSignedReady.getReady();
        SignedLocationReport signedReport = ready.getSignedLocationReport();

        if(!isValidPoW(ready)){
            return ReadyResponse.newBuilder().build();
        }

        if (this.nonceSet.contains(ready.getNonce())) {
            return ReadyResponse.newBuilder().build();
        }

        this.nonceSet.add(ready.getNonce());
        this.nonceWriteQueue.write(ready.getNonce());

        if(!verifyServerSignature(ready.getServerId(), serverSignedReady.getReady().toByteArray(),
                serverSignedReady.getSignature().toByteArray())){
            return ReadyResponse.newBuilder().build();
        }

        if(!verifySignature(signedReport.getLocationReport().getLocationInformation().getUserId(),
                signedReport.getLocationReport().toByteArray(),
                signedReport.getUserSignature().toByteArray())){
            return ReadyResponse.newBuilder().build();
        }

        BroadcastVars broadcastVars = new BroadcastVars();
        broadcastVars.getReadys().add(serverSignedReady);
        BroadcastVars aux = broadcast.putIfAbsent(signedReport, broadcastVars);
        if(aux != null){
            aux.addReady(serverSignedReady);
            broadcastVars = aux;
        }

        if(broadcastVars.getReadys().size() > F && !broadcastVars.getSentReady()){
            broadcastVars.setSentReady(true);
            submitReady(signedReport);
        }

        if(broadcastVars.getReadys().size() > 2*F && !broadcastVars.getDelivered()){
            broadcastVars.setDelivered(true);
            LocationInformation locationInformation =  signedReport.getLocationReport().getLocationInformation();
            deliver(signedReport, new LocationReportKey(locationInformation.getUserId(), locationInformation.getEpoch()));
            broadcastVars.freeBlocker();
        }

        return ReadyResponse.newBuilder().build();
    }

    private byte[] decryptKey(byte[] key, PrivateKey privateKey) throws Exception {
        return CryptographicUtils.asymmetricDecrypt(key, privateKey);
    }

    private byte[] decryptRequest(byte[] request, byte[] key, byte[] iv) throws Exception {
        return CryptographicUtils.symmetricDecrypt(request,
                CryptographicUtils.convertToSymmetricKey(key), new IvParameterSpec(iv));
    }

    private byte[] encryptResponse(byte[] data, byte[] secretKey, IvParameterSpec iv) throws Exception {
        return CryptographicUtils.symmetricEncrypt(data, CryptographicUtils.convertToSymmetricKey(secretKey), iv);
    }

    private boolean validReport(SignedLocationReport signedReport, int numberByzantineUsers) throws Exception {

        if (!verifySignature(signedReport.getLocationReport().getLocationInformation().getUserId(),
                signedReport.getLocationReport().toByteArray(), signedReport.getUserSignature().toByteArray())) {
            return false;
        }

        HashSet<Integer> witnessIds = new HashSet<>();

        if (signedReport.getLocationReport().getLocationProofList().size() <= numberByzantineUsers) {
            return false;
        }

        for (SignedLocationProof sProof : signedReport.getLocationReport().getLocationProofList()) {
            LocationProof lProof = sProof.getLocationProof();

            if (witnessIds.contains(lProof.getWitnessId())) {
                return false;
            }

            witnessIds.add(lProof.getWitnessId());

            if (!verifySignature(lProof.getWitnessId(), lProof.toByteArray(), sProof.getSignature().toByteArray())) {
                return false;
            }

            if (!verifyLocationProof(signedReport.getLocationReport().getLocationInformation(), lProof)) {
                return false;
            }
        }

        return true;
    }

    private boolean verifySignature(int userId, byte[] message, byte[] signature) throws Exception {
        return CryptographicUtils.verifySignature(FileUtils.getUserPublicKey(userId), message, signature);
    }

    private boolean verifyServerSignature(int serverId, byte[] message, byte[] signature) throws Exception {
        return CryptographicUtils.verifySignature(FileUtils.getServerPublicKey(serverId), message, signature);
    }

    private boolean verifyLocationProof(LocationInformation lInfo, LocationProof lProof) {
        return lInfo.getUserId() == lProof.getProverId() &&
                lInfo.getEpoch() == lProof.getEpoch() &&
                Math.abs(lInfo.getPosition().getX() - lProof.getPosition().getX()) <= 15 &&
                Math.abs(lInfo.getPosition().getY() - lProof.getPosition().getY()) <= 15;
    }

    private boolean verifyHaSignature(byte[] message, byte[] signature) throws Exception {
        return CryptographicUtils.verifySignature(FileUtils.getHAPublicKey(), message, signature);
    }

    public void terminateMessageWriteQueue() throws InterruptedException {
        this.messageWriteQueue.terminate();
    }

    public void terminateNonceWriteQueue() throws InterruptedException {
        this.nonceWriteQueue.terminate();
    }
}
