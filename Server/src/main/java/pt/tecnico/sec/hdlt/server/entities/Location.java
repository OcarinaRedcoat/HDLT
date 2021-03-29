package pt.tecnico.sec.hdlt.server.entities;

public class Location {

    private String userId;
    private int epoch;
    private Position position;

    public Location(String userId, int epoch, Position position) {
        this.userId = userId;
        this.epoch = epoch;
        this.position = position;
    }

    public String getUserId() {
        return userId;
    }

    public int getEpoch() {
        return epoch;
    }

    public Position getPosition() {
        return position;
    }
}
