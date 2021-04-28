package pt.tecnico.sec.hdlt.client;

import pt.tecnico.sec.hdlt.client.communication.UserClient;
import pt.tecnico.sec.hdlt.client.communication.UserServer;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.LocationReport;

import java.security.PrivateKey;

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
        System.out.println("(4) Current f");
        System.out.println("(5) Exit");
        System.out.println("-----------------------------");
    }

    //     ../grids.output.json
    public static void main(String[] args) {
        Client client = new Client(readUser());
        //TODO change F
        int f = readF();
        UserServer serverGrpc = new UserServer(client);
        UserClient clientGrpc = new UserClient();

        int command;
        long epoch;
        LocationReport report;

        System.out.println("||| CLIENT INITIALIZED |||");
        printCommands();
        do{
            command = readInteger(null);
            switch (command){
                case 1:
                    epoch = readEpoch();
                    report = clientGrpc.requestLocationProofs(client, epoch, f);
                    if(report != null)
                        clientGrpc.submitLocationReport(client, report);
                    break;
                case 2:
                    epoch = readEpoch();
                    clientGrpc.obtainLocationReport(client, epoch);
                    break;
                case 3:
                    System.out.println("-----> Current Epoch: " + getCurrentEpoch());
                    break;
                case 4:
                    System.out.println("-----> Current f: " + f);
                    break;
                case 5:
                    break;
                default:
                    System.out.println("-----> Invalid Command.");
            }
            printCommands();
        }while(command != 5);

        serverGrpc.stop();
    }

}
