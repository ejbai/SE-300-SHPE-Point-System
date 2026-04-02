import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PointsSystem {
    public static String checkLogin(String username, String password) {
        try (Connection conn = DatabaseConnection.connect()) {
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

    public static ResultSet showUpcomingEvents() {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT name, location, timeAndDate, pointsNeeded, pointsEarned FROM events WHERE timeAndDate >= datetime('now') ORDER BY timeAndDate ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {    // TODO: problem is that this is getting skipped because rs doesn't return anything. don't know why yet
                return rs;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ResultSet showAllEvents() {
        try (Connection conn = DatabaseConnection.connect()) {
            String sql = "SELECT name, location, timeAndDate, pointsNeeded, pointsEarned FROM events ORDER BY timeAndDate ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
