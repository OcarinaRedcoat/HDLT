package pt.tecnico.sec.hdlt.client;

import pt.tecnico.sec.hdlt.client.communication.UserServer;

import static pt.tecnico.sec.hdlt.client.utils.IOUtils.*;

/**
 * Hello world!
 *
 */
public class Main
{
    //TODO: G:\IST\2-Semestre\SEC\Projeto\HDLT\grids.output.json
    public static void main(String[] args) {
        setGridFile();

        readUser();

        UserServer.getInstance().start();

        startUserInteraction();

        UserServer.getInstance().stop();
    }

}
