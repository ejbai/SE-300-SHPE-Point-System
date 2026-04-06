import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PointsSystem {
    public static ResultSet checkLogin(Connection conn, String username, String password) {
        try {
            String sql = "SELECT studentID, userRank FROM accounts WHERE username=? AND passwordHashSHA256=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs;   // return basic member information if login works
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // login failed
    }

    public static ResultSet createUser(Connection conn, int studentID) {
        try {
            String sql = "SELECT * FROM members WHERE studentID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentID);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ResultSet showUpcomingEvents(Connection conn) {
        try {
            String sql = "SELECT name, location, timeAndDate, pointsNeeded, pointsEarned FROM events WHERE timeAndDate >= datetime('now') ORDER BY timeAndDate ASC";
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

    public static ResultSet showAllEvents(Connection conn) {
        try {
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

    public static ResultSet viewMemberNameAndPoints(Connection conn, int studentID) {
        try {
            String sql = "SELECT firstName, lastName, points FROM members WHERE studentID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentID);
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
