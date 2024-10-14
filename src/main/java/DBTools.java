import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBTools {
    public static Connection connect() throws ClassNotFoundException, SQLException {
        String jdbcUrl = System.getenv("IGROLION_DB_JDBC_ADDR");
        String userName = System.getenv("IGROLION_DB_USER");
        String password = System.getenv("IGROLION_DB_PASSWORD");

        Class.forName("org.postgresql.Driver");
        return DriverManager.getConnection(jdbcUrl, userName, password);
    }

    public void close(Connection con) throws SQLException {
        con.close();
    }
}
