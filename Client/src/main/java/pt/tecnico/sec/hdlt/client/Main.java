package pt.tecnico.sec.hdlt.client;

import pt.tecnico.sec.hdlt.client.communication.UserClient;
import pt.tecnico.sec.hdlt.client.communication.UserServer;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.LocationReport;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

import static pt.tecnico.sec.hdlt.FileUtils.getUserPublicKey;
import static pt.tecnico.sec.hdlt.GeneralUtils.F;
import static pt.tecnico.sec.hdlt.GeneralUtils.getCurrentEpoch;
import static pt.tecnico.sec.hdlt.IOUtils.*;

public class Main
{
    private static void printCommands(){
        System.out.println("-----------------------------");
        System.out.println("Available commands:");
        System.out.println("(1) Submit a report");
        System.out.println("(2) Obtain a report");
        System.out.println("(3) Current epoch");
        System.out.println("(4) Defined F");
        System.out.println("(5) Exit");
        System.out.println("-----------------------------");
    }

    //     ../grids.output.json
    public static void main(String[] args) {
        Client client = new Client(readUser());
        UserServer serverGrpc = new UserServer(client);
        UserClient clientGrpc = new UserClient();

        int command;

        System.out.println("||| CLIENT INITIALIZED |||");
        do{
            printCommands();
            command = readInteger(null);
            switch (command){
                case 1:
                    LocationReport.Builder reportBuilder = clientGrpc.requestLocationProofs(client, readEpoch(), F);
                    if(reportBuilder != null)
                        clientGrpc.submitLocationReport(client, reportBuilder);
                    break;
                case 2:
                    clientGrpc.obtainLocationReport(client, readEpoch());
                    break;
                case 3:
                    System.out.println("-----> Current Epoch: " + getCurrentEpoch());
                    break;
                case 4:
                    System.out.println("-----> Defined F = " + F);
                    break;
                case 5:
                    break;
                default:
                    System.out.println("-----> Invalid Command.");
            }
        }while(command != 5);

        serverGrpc.stop();
    }

}
