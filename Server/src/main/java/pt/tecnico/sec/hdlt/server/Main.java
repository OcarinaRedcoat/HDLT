package pt.tecnico.sec.hdlt.server;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;
import pt.tecnico.sec.hdlt.server.dal.LocationDAO;

import java.io.IOException;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        LocationDAO locationDAO = new LocationDAO();
        locationDAO.getUser();

        AbandonedConnectionCleanupThread.checkedShutdown();
//        System.exit(0);
    }
}
