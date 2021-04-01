package pt.tecnico.sec.hdlt.server.utils;

import pt.tecnico.sec.hdlt.communication.LocationReport;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingDeque;

public class WriteQueue {

    private Queue<LocationReport> writes;

    public WriteQueue() {
        this.writes = new LinkedBlockingDeque<>();

//        new Thread()
    }
}
