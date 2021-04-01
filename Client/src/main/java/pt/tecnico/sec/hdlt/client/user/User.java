package pt.tecnico.sec.hdlt.client.user;

import com.sun.jmx.remote.internal.ArrayQueue;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.InvalidParameterException;
import java.util.ArrayList;

public class User {

    private int port;
    private String host;

    private long id;
    private ArrayList<Position> positions;

    public User(long id, String ip, int port){
        this.id = id;
        this.host = ip;
        this.port = port;
        this.positions = new ArrayList<>();
    }

    public void setId(long id){ this.id = id; }

    public void setIp(String ip){ this.host = ip; }

    public void setPort(int port){ this.port = port; }

    public long getId(){ return this.id; }

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
        throw new InvalidParameterException(); //TODO fazer uma exception propria
    }
}
