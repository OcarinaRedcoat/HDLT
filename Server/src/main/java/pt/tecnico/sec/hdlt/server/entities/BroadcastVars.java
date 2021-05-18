package pt.tecnico.sec.hdlt.server.entities;

import pt.tecnico.sec.hdlt.communication.EchoRequest;
import pt.tecnico.sec.hdlt.communication.ReadyRequest;

import java.util.ArrayList;
import java.util.List;

public class BroadcastVars {
    private Boolean sentEcho;
    private Boolean sentReady;
    private Boolean delivered;
    private List<EchoRequest> echos;
    private List<ReadyRequest> ready;

    public BroadcastVars() {
        this.sentEcho = false;
        this.sentReady = false;
        this.delivered = false;
        this.echos = new ArrayList<>();
        this.ready = new ArrayList<>();
    }
}
