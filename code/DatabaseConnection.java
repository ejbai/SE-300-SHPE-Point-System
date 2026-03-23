import java.sql.*;

public class DatabaseConnection {

    private static final String URL = "jdbc:mariadb://localhost:3306/shpe_db";
    private static final String USER = "user1";
    private static final String PASS = "";

    public static String checkLogin(String username, String password) {

        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {

            String sql = "SELECT userRank FROM accounts WHERE username=? AND passwordHash=?";
            PreparedStatement stmt = conn.prepareStatement(sql);

            stmt.setString(1, username);
            stmt.setString(2, password);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("userRank");   // return rank if login works
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null; // login failed
    }

    static void main() {
        checkLogin("Alice", "6b3a55e0261b0304143f805a24924d0c1c44524821305f31d9277843b8a10f4e");
    }
}
