package pt.tecnico.sec.hdlt.client;

import com.sun.jmx.remote.internal.ArrayQueue;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;

public class User {

    private int id;
    private int port;
    private String host;


    private int x_position;
    private int y_position;
    private ArrayList<Long> closeBy;

    protected User(int id){
        this.id = id;
    }

    protected User(int id, String ip, int port, int xPos, int yPos, ArrayList<Long> closeBy){
        this.id = id;
        this.host = ip;
        this.port = port;
        this.x_position = xPos;
        this.y_position = yPos;
        this.closeBy = closeBy;
    }

    protected void setId(int id){ this.id = id; }

    protected void setIp(String ip){ this.host = ip; }

    protected void setPort(int port){ this.port = port; }

    protected int getId(){ return this.id; }

    protected String getHost(){ return this.host; }

    protected int getPort(){ return this.port; }

    public void setHost(String host) { this.host = host; }

    public int getX_position() { return x_position; }

    public void setX_position(int x_position) { this.x_position = x_position; }

    public int getY_position() { return y_position; }

    public void setY_position(int y_position) { this.y_position = y_position; }

    public ArrayList<Long> getCloseBy() { return closeBy; }

    public void setCloseBy(ArrayList<Long> closeBy) { this.closeBy = closeBy; }


}
