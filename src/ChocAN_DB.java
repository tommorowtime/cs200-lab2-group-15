import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class ChocAN_DB {
    private static String dbUrl;
    private static boolean initialized = false;

    private static synchronized String getDbUrl() {
        if (dbUrl != null) {
            return dbUrl;
        }
        String envUrl = System.getenv("DB_URL");
        if (envUrl != null && !envUrl.isBlank()) {
            dbUrl = envUrl.trim();
            return dbUrl;
        }
        String[] candidatePaths = {"db.properties", "src/db.properties", "../db.properties"};
        for (String path : candidatePaths) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(path)) {
                props.load(fis);
                String url = props.getProperty("DB_URL");
                if (url != null && !url.isBlank()) {
                    dbUrl = url.trim();
                    return dbUrl;
                }
            } catch (IOException e) {
                // Try next candidate path
            }
        }
        throw new IllegalStateException(
                "Missing database URL. Create a 'db.properties' file with DB_URL=... or set the DB_URL environment variable.");
    }

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found on classpath (lib/postgresql-42.7.13.jar).", e);
        }
        return DriverManager.getConnection(getDbUrl());
    }

    public static synchronized boolean initializeDatabase() {
        if (initialized) {
            return true;
        }
        String createMemberTable = """
                CREATE TABLE IF NOT EXISTS public."Member" (
                    "MemberId" VARCHAR(9) PRIMARY KEY,
                    "Name" TEXT NOT NULL,
                    "Address" TEXT NOT NULL DEFAULT '',
                    "City" TEXT NOT NULL DEFAULT '',
                    "State" VARCHAR(2) NOT NULL DEFAULT '',
                    "Zip" VARCHAR(10) NOT NULL DEFAULT '',
                    "Suspended" BOOLEAN NOT NULL DEFAULT FALSE
                );
                """;

        String createProviderTable = """
                CREATE TABLE IF NOT EXISTS public."Provider" (
                    "ProviderId" VARCHAR(9) PRIMARY KEY,
                    "Name" TEXT NOT NULL,
                    "Address" TEXT NOT NULL DEFAULT '',
                    "City" TEXT NOT NULL DEFAULT '',
                    "State" VARCHAR(2) NOT NULL DEFAULT '',
                    "Zip" VARCHAR(10) NOT NULL DEFAULT ''
                );
                """;

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(createMemberTable);
            stmt.execute(createProviderTable);
            initialized = true;
            return true;
        } catch (Exception e) {
            System.err.println("Error initializing database tables: " + e.getMessage());
            return false;
        }
    }

    // ==================== OPERATOR AUTH & MANAGEMENT ====================

    public static boolean authenticateOperator(String name, String secret) {
        initializeDatabase();
        String sql = "SELECT 1 FROM public.\"Operator\" WHERE \"OperatorName\" = ? AND \"OperatorSecret\" = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, secret);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (Exception e) {
            System.err.println("Error authenticating operator against DB: " + e.getMessage());
            return false;
        }
    }

    public static void addOperator(String name, String secret) {
        initializeDatabase();
        String sql = "INSERT INTO public.\"Operator\" (\"OperatorName\", \"OperatorSecret\") VALUES (?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setString(2, secret);
            pstmt.executeUpdate();
            System.out.println("Added operator: " + name);

        } catch (Exception e) {
            System.err.println("Error adding operator: " + e.getMessage());
        }
    }

    public static void listOperators() {
        initializeDatabase();
        String sql = "SELECT id, \"OperatorName\" FROM public.\"Operator\" ORDER BY id";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("Current Operators:");
            while (rs.next()) {
                System.out.println("- [" + rs.getInt("id") + "] " + rs.getString("OperatorName"));
            }

        } catch (Exception e) {
            System.err.println("Error querying operators: " + e.getMessage());
        }
    }

    // ==================== MEMBER CRUD ====================

    public static boolean addMember(Member member) {
        initializeDatabase();
        String sql = """
                INSERT INTO public."Member" ("MemberId", "Name", "Address", "City", "State", "Zip", "Suspended")
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, member.getMemberId());
            pstmt.setString(2, member.getName());
            pstmt.setString(3, member.getAddress());
            pstmt.setString(4, member.getCity());
            pstmt.setString(5, member.getState());
            pstmt.setString(6, member.getZip());
            pstmt.setBoolean(7, member.isSuspended());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error adding member to DB: " + e.getMessage());
            return false;
        }
    }

    public static Member getMemberById(String memberId) {
        initializeDatabase();
        String sql = """
                SELECT "MemberId", "Name", "Address", "City", "State", "Zip", "Suspended"
                FROM public."Member"
                WHERE "MemberId" = ?
                """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, memberId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Member(
                            rs.getString("MemberId"),
                            rs.getString("Name"),
                            rs.getString("Address"),
                            rs.getString("City"),
                            rs.getString("State"),
                            rs.getString("Zip"),
                            rs.getBoolean("Suspended")
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching member from DB: " + e.getMessage());
        }
        return null;
    }

    public static List<Member> getAllMembers() {
        initializeDatabase();
        List<Member> list = new ArrayList<>();
        String sql = """
                SELECT "MemberId", "Name", "Address", "City", "State", "Zip", "Suspended"
                FROM public."Member"
                ORDER BY "MemberId"
                """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Member(
                        rs.getString("MemberId"),
                        rs.getString("Name"),
                        rs.getString("Address"),
                        rs.getString("City"),
                        rs.getString("State"),
                        rs.getString("Zip"),
                        rs.getBoolean("Suspended")
                ));
            }
        } catch (Exception e) {
            System.err.println("Error listing members from DB: " + e.getMessage());
        }
        return list;
    }

    public static boolean updateMember(Member member) {
        initializeDatabase();
        String sql = """
                UPDATE public."Member"
                SET "Name" = ?, "Address" = ?, "City" = ?, "State" = ?, "Zip" = ?, "Suspended" = ?
                WHERE "MemberId" = ?
                """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, member.getName());
            pstmt.setString(2, member.getAddress());
            pstmt.setString(3, member.getCity());
            pstmt.setString(4, member.getState());
            pstmt.setString(5, member.getZip());
            pstmt.setBoolean(6, member.isSuspended());
            pstmt.setString(7, member.getMemberId());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error updating member in DB: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteMember(String memberId) {
        initializeDatabase();
        String sql = "DELETE FROM public.\"Member\" WHERE \"MemberId\" = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, memberId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting member from DB: " + e.getMessage());
            return false;
        }
    }

    // ==================== PROVIDER CRUD ====================

    public static boolean addProvider(Provider provider) {
        initializeDatabase();
        String sql = """
                INSERT INTO public."Provider" ("ProviderId", "Name", "Address", "City", "State", "Zip")
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, provider.getProviderId());
            pstmt.setString(2, provider.getName());
            pstmt.setString(3, provider.getAddress());
            pstmt.setString(4, provider.getCity());
            pstmt.setString(5, provider.getState());
            pstmt.setString(6, provider.getZip());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error adding provider to DB: " + e.getMessage());
            return false;
        }
    }

    public static Provider getProviderById(String providerId) {
        initializeDatabase();
        String sql = """
                SELECT "ProviderId", "Name", "Address", "City", "State", "Zip"
                FROM public."Provider"
                WHERE "ProviderId" = ?
                """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, providerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Provider(
                            rs.getString("ProviderId"),
                            rs.getString("Name"),
                            rs.getString("Address"),
                            rs.getString("City"),
                            rs.getString("State"),
                            rs.getString("Zip")
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Error fetching provider from DB: " + e.getMessage());
        }
        return null;
    }

    public static List<Provider> getAllProviders() {
        initializeDatabase();
        List<Provider> list = new ArrayList<>();
        String sql = """
                SELECT "ProviderId", "Name", "Address", "City", "State", "Zip"
                FROM public."Provider"
                ORDER BY "ProviderId"
                """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Provider(
                        rs.getString("ProviderId"),
                        rs.getString("Name"),
                        rs.getString("Address"),
                        rs.getString("City"),
                        rs.getString("State"),
                        rs.getString("Zip")
                ));
            }
        } catch (Exception e) {
            System.err.println("Error listing providers from DB: " + e.getMessage());
        }
        return list;
    }

    public static boolean updateProvider(Provider provider) {
        initializeDatabase();
        String sql = """
                UPDATE public."Provider"
                SET "Name" = ?, "Address" = ?, "City" = ?, "State" = ?, "Zip" = ?
                WHERE "ProviderId" = ?
                """;
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, provider.getName());
            pstmt.setString(2, provider.getAddress());
            pstmt.setString(3, provider.getCity());
            pstmt.setString(4, provider.getState());
            pstmt.setString(5, provider.getZip());
            pstmt.setString(6, provider.getProviderId());
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error updating provider in DB: " + e.getMessage());
            return false;
        }
    }

    public static boolean deleteProvider(String providerId) {
        initializeDatabase();
        String sql = "DELETE FROM public.\"Provider\" WHERE \"ProviderId\" = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, providerId);
            return pstmt.executeUpdate() > 0;
        } catch (Exception e) {
            System.err.println("Error deleting provider from DB: " + e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        initializeDatabase();
        listOperators();
    }
}
