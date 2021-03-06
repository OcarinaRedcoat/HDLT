package pt.tecnico.sec.hdlt.entities;

import java.security.InvalidParameterException;
import java.util.ArrayList;

public class User {

    private int port;
    private String host;

    private int id;
    private ArrayList<Position> positions;

    public User(int id, String ip, int port){
        this.id = id;
        this.host = ip;
        this.port = port;
        this.positions = new ArrayList<>();
    }

    public void setId(int id){ this.id = id; }

    public void setIp(String ip){ this.host = ip; }

    public void setPort(int port){ this.port = port; }

    public int getId(){ return this.id; }

    public String getHost(){ return this.host; }

    public int getPort(){ return this.port; }

    public void setHost(String host) { this.host = host; }

    public ArrayList<Position> getPositions() {
        return positions;
    }

    public void setPositions(ArrayList<Position> positions) {
        this.positions = positions;
    }

    public void addPosition(Position position){
        this.positions.add(position);
    }

    public Position getPositionWithEpoch(long epoch) throws InvalidParameterException {
        for (Position pos : positions) {
            if (pos.getEpoch() == epoch) {
                return pos;
            }
        }
        throw new InvalidParameterException("Invalid Epoch");
    }

    public Boolean isCloseTo(long closeId, long epoch){
        for (Long id : this.getPositionWithEpoch(epoch).getCloseBy()) {
            if(id == closeId){
                return true;
            }
        }
        return false;
    }
}
