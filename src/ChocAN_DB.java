import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class ChocAN_DB {
    private static final String DB_URL = loadDbUrl();

    private static String loadDbUrl() {
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            return envUrl;
        }
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream("db.properties")) {
            props.load(fis);
            String url = props.getProperty("DB_URL");
            if (url != null && !url.isBlank()) {
                return url.trim();
            }
        } catch (IOException e) {
            // Fall through to error message below
        }
        throw new IllegalStateException(
                "Missing database URL. Create a 'db.properties' file with DB_URL=... or set the DB_URL environment variable.");
    }

    public static void main(String[] args) {
        addOperator("Jerry", "HersheyBar15");
        listOperators();
    }

    public static void addOperator(String name, String secret) {
        String sql = "INSERT INTO public.\"Operator\" (\"OperatorName\", \"OperatorSecret\") VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, secret);
            pstmt.executeUpdate();
            System.out.println("Added operator: " + name);

        } catch (SQLException e) {
            System.err.println("Error adding operator: " + e.getMessage());
        }
    }

    public static void listOperators() {
        String sql = "SELECT id, \"OperatorName\" FROM public.\"Operator\"";

        try (Connection conn = DriverManager.getConnection(DB_URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Current Operators:");
            while (rs.next()) {
                System.out.println("- [" + rs.getInt("id") + "] " + rs.getString("OperatorName"));
            }

        } catch (SQLException e) {
            System.err.println("Error querying operators: " + e.getMessage());
        }
    }
}
