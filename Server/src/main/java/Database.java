import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetFactory;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class Database {

    private static final String userName = "root";
    private static final String password = "root";
    private static final String connectionURL = "jdbc:mysql://localhost:3306/learning?verifyServerCertificate=false&useSSL=false&useUnicode=true&characterEncoding=utf-8&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";
    private static Connection connection;


    public static void Connect() throws ClassNotFoundException {

        Class.forName("com.mysql.cj.jdbc.Driver");

        try {
             connection = DriverManager.getConnection(connectionURL, userName, password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addMessage(String message) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO messages(message) values (?)");
            preparedStatement.setString(1,message);
            preparedStatement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static ArrayList<String> getLastMessages() {
        try {
            Statement statement = connection.createStatement();
           ResultSet resultSet = statement.executeQuery("SELECT * from messages order by id desc limit 10");
            ArrayList<String> result = new ArrayList<>();
            while (resultSet.next()) {
                String message = resultSet.getString("message");
                Timestamp timeStamp = resultSet.getTimestamp("sending_time");

                result.add(timeStamp + ": " + message);
            }
            Collections.reverse(result); // reversing arraylist to make normal ordering
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void Disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
