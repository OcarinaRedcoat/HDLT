package pt.tecnico.sec.hdlt.server.entities;

import pt.tecnico.sec.hdlt.communication.EchoRequest;
import pt.tecnico.sec.hdlt.communication.ReadyRequest;
import pt.tecnico.sec.hdlt.communication.ServerSignedEcho;
import pt.tecnico.sec.hdlt.communication.ServerSignedReady;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BroadcastVars {
    private Boolean sentEcho;
    private Boolean sentReady;
    private Boolean delivered;
    private CopyOnWriteArrayList<ServerSignedEcho> echos;
    private CopyOnWriteArrayList<ServerSignedReady> ready;

    public BroadcastVars() {
        this.sentEcho = false;
        this.sentReady = false;
        this.delivered = false;
        this.echos = new CopyOnWriteArrayList<>();
        this.ready = new CopyOnWriteArrayList<>();
    }

    public Boolean getSentEcho() {
        return sentEcho;
    }

    public void setSentEcho(Boolean sentEcho) {
        this.sentEcho = sentEcho;
    }

    public Boolean getSentReady() {
        return sentReady;
    }

    public void setSentReady(Boolean sentReady) {
        this.sentReady = sentReady;
    }

    public Boolean getDelivered() {
        return delivered;
    }

    public void setDelivered(Boolean delivered) {
        this.delivered = delivered;
    }

    public CopyOnWriteArrayList<ServerSignedEcho> getEchos() {
        return echos;
    }

    public void setEchos(CopyOnWriteArrayList<ServerSignedEcho> echos) {
        this.echos = echos;
    }

    public CopyOnWriteArrayList<ServerSignedReady> getReady() {
        return ready;
    }

    public void setReady(CopyOnWriteArrayList<ServerSignedReady> ready) {
        this.ready = ready;
    }
}
