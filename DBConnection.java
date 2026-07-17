import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    public static Connection getConnection() {

        Connection con = null;

        try {
Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
            con = DriverManager.getConnection(
                "jdbc:ucanaccess://D:/JAVA_SPARKS/JAVA_SPARKS/DataBase/library.accdb"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        return con;
    }
}