package pt.tecnico.sec.hdlt.server.dal;

import com.mysql.cj.jdbc.MysqlDataSource;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public abstract class DAO {
    protected MysqlDataSource dataSource;

    public DAO() throws IOException, ClassNotFoundException {
        Properties prop = new Properties();
        prop.load(new FileInputStream("src/main/java/pt/tecnico/sec/hdlt/server/db/database.properties"));

        dataSource = new MysqlDataSource();
        dataSource.setUser(prop.getProperty("dbUser"));
        dataSource.setPassword(prop.getProperty("dbPassword"));
        dataSource.setURL(prop.getProperty("dbUrl"));
    }
}
