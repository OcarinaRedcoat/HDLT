package pt.tecnico.sec.hdlt.client;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class User {

    private int id;
    private int port;
    private String host;

    protected User(int id){
        this.id = id;
    }

    protected User(int id, String ip, int port){
        this.id = id;
        this.host = ip;
        this.port = port;
    }

    protected void setId(int id){ this.id = id; }

    protected void setIp(String ip){ this.host = ip; }

    protected void setPort(int port){ this.port = port; }

    protected int getId(){ return this.id; }

    protected String getHost(){ return this.host; }

    protected int getPort(){ return this.port; }

}
