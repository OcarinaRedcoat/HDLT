package pt.tecnico.sec.hdlt.client;

import pt.tecnico.sec.hdlt.client.communication.UserClient;
import pt.tecnico.sec.hdlt.client.communication.UserServer;
import pt.tecnico.sec.hdlt.client.user.Client;
import pt.tecnico.sec.hdlt.communication.LocationReport;

import static pt.tecnico.sec.hdlt.GeneralUtils.getCurrentEpoch;
import static pt.tecnico.sec.hdlt.IOUtils.*;

public class Main
{
    //     ../grids.output.json
    public static void main(String[] args) {
        Client client = new Client(readUser());
        int f = readF();
        UserServer serverGroc = new UserServer(client);
        UserClient clientGrpc = new UserClient();

        String command;
        long epoch;
        LocationReport report;

        System.out.println("Client Initialized. Type \"help\" at any point for the list of available commands.");
        do{
            command = readString(null);
            switch (command){
                case "help":
                    System.out.println("Available commands: \"help\", \"submit report\"(it will also request witnesses automatically), " +
                            "\"current epoch\", \"redefine f\",\"current f\", \"obtain report\", \"exit\".");
                    break;
                case "submit report":
                    epoch = readEpoch();
                    report = clientGrpc.requestLocationProofs(client, epoch, f);
                    if(report != null)
                        clientGrpc.submitLocationReport(client, report);
                    break;
                case "obtain report":
                    epoch = readEpoch();
                    clientGrpc.obtainLocationReport(client, epoch);
                    break;
                case "current epoch":
                    System.out.println("Current Epoch: " + getCurrentEpoch());
                    break;
                case "redefine f":
                    f = readF();
                    break;
                case "current f":
                    System.out.println("Current f: " + f);
                    break;
                case "exit":
                    break;
                default:
                    System.out.println("Invalid Command. Type \"help\" for available commands.");
            }
        }while(!command.equals("exit"));

        serverGroc.stop();
    }

}
