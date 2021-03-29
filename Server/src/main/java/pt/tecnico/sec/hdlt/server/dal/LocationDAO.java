package pt.tecnico.sec.hdlt.server.dal;

import com.mysql.cj.jdbc.AbandonedConnectionCleanupThread;

import java.io.IOException;
import java.sql.*;

public class LocationDAO extends DAO {

    public LocationDAO() throws IOException, ClassNotFoundException {
    }

    public void getUser() throws SQLException {
        Connection conn = dataSource.getConnection();

        String query = "SELECT * FROM users WHERE username='ANDRE'";
        PreparedStatement stmt = conn.prepareStatement(query);
        ResultSet rst = stmt.executeQuery();

        if (rst.next()) {
            System.out.println(rst.getString("username") + " - " + rst.getString("age"));
        }

        rst.close();
        stmt.close();
        conn.close();
    }
}
