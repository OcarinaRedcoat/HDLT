package pt.tecnico.sec.hdlt.entities;

import java.util.ArrayList;

public class Position {
    private long epoch;
    private long xPos;
    private long yPos;
    private ArrayList<Long> closeBy;

    public Position(long epoch, long xPos, long yPos) {
        this.epoch = epoch;
        this.xPos = xPos;
        this.yPos = yPos;
        this.closeBy = new ArrayList<>();
    }

    public ArrayList<Long> getCloseBy() {
        return closeBy;
    }

    public void setCloseBy(ArrayList<Long> closeBy) {
        this.closeBy = closeBy;
    }

    public long getEpoch() {
        return epoch;
    }

    public void setEpoch(long epoch) {
        this.epoch = epoch;
    }

    public long getxPos() {
        return xPos;
    }

    public void setxPos(long xPos) {
        this.xPos = xPos;
    }

    public long getyPos() {
        return yPos;
    }

    public void setyPos(long yPos) {
        this.yPos = yPos;
    }
}
