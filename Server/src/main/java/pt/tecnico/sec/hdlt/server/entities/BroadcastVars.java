package pt.tecnico.sec.hdlt.server.entities;

import pt.tecnico.sec.hdlt.communication.EchoRequest;
import pt.tecnico.sec.hdlt.communication.ReadyRequest;
import pt.tecnico.sec.hdlt.communication.ServerSignedEcho;
import pt.tecnico.sec.hdlt.communication.ServerSignedReady;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

import static pt.tecnico.sec.hdlt.utils.GeneralUtils.F;
import static pt.tecnico.sec.hdlt.utils.GeneralUtils.N_SERVERS;

public class BroadcastVars {
    private Boolean sentEcho;
    private Boolean sentReady;
    private Boolean delivered;
    private CopyOnWriteArrayList<ServerSignedEcho> echos;
    private CopyOnWriteArrayList<ServerSignedReady> readys;
    private CountDownLatch blocker;

    public BroadcastVars() {
        this.sentEcho = false;
        this.sentReady = false;
        this.delivered = false;
        this.blocker = new CountDownLatch(1);
        this.echos = new CopyOnWriteArrayList<>();
        this.readys = new CopyOnWriteArrayList<>();
    }

    public synchronized Boolean getSentEcho() {
        return sentEcho;
    }

    public synchronized Boolean setSentEcho(Boolean sentEcho) {
        Boolean aux = this.sentEcho;
        this.sentEcho = sentEcho;
        return aux;
    }

    public synchronized Boolean getSentReady() {
        return sentReady;
    }

    public synchronized void setSentReady(Boolean sentReady) {
        this.sentReady = sentReady;
    }

    public synchronized Boolean getDelivered() {
        return delivered;
    }

    public synchronized void setDelivered(Boolean delivered) {
        this.delivered = delivered;
    }

    public CopyOnWriteArrayList<ServerSignedEcho> getEchos() {
        return echos;
    }

    public void setEchos(CopyOnWriteArrayList<ServerSignedEcho> echos) {
        this.echos = echos;
    }

    public CopyOnWriteArrayList<ServerSignedReady> getReadys() {
        return readys;
    }

    public void setReady(CopyOnWriteArrayList<ServerSignedReady> ready) {
        this.readys = ready;
    }

    public synchronized void addEcho(ServerSignedEcho serverSignedEcho){
        for (ServerSignedEcho aux: echos){
            if(aux.getEcho().getServerId() == serverSignedEcho.getEcho().getServerId()){
                return;
            }
        }
        echos.add(serverSignedEcho);
    }

    public synchronized void addReady(ServerSignedReady serverSignedReady){
        for (ServerSignedReady aux: readys){
            if(aux.getReady().getServerId() == serverSignedReady.getReady().getServerId()){
                return;
            }
        }
        readys.add(serverSignedReady);
    }

    public CountDownLatch getBlocker() {
        return blocker;
    }

    public void freeBlocker() {
        blocker.countDown();
    }
}
