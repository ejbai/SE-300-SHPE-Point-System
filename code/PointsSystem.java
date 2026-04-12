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
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ResultSet createUser(Connection conn, int studentID) {
        try {
            String sql = "SELECT * FROM members WHERE studentID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentID);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ResultSet showUpcomingEvents(Connection conn) {
        try {
            String sql = "SELECT name, location, timeAndDate, pointsNeeded, pointsEarned " +
                    "FROM events " +
                    "WHERE timeAndDate >= datetime('now') " +
                    "ORDER BY timeAndDate ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ResultSet showAllEvents(Connection conn) {
        try {
            String sql = "SELECT name, location, timeAndDate, pointsNeeded, pointsEarned " +
                    "FROM events ORDER BY timeAndDate ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ResultSet viewCurrentMemberNameAndPoints(Connection conn, int studentID) {
        try {
            String sql = "SELECT firstName, lastName, points FROM members WHERE studentID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentID);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ResultSet viewAllMemberContacts(Connection conn) {
        try {
            String sql = "SELECT firstName, lastName, email, phoneNumber FROM members ORDER BY lastName ASC, firstName ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet searchForMember(Connection conn, int studentID) {
        try {
            String sql = "SELECT * FROM members WHERE studentID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentID);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ResultSet viewAllMembers(Connection conn) {
        try {
            String sql = "SELECT * FROM members ORDER BY lastName ASC, firstName ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addMember(Connection conn, int studentID, String firstName, String lastName, String email, String phoneNumber, int points) {
        try {
            String sql = "SELECT studentID FROM members WHERE studentID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentID);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            String sql = "INSERT INTO members VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentID);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, email);
            stmt.setString(5, phoneNumber);
            stmt.setInt(6, points);
            stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void editMember(Connection conn, int studentID, String firstName, String lastName, String email, String phoneNumber, int points) {
        // TODO
    }

    public static void deleteMember(Connection conn, int studentID) {
        try {
            String sql = "SELECT studentID FROM members WHERE studentID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentID);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            String sql = "DELETE FROM members WHERE studentID = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentID);
            stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addEvent(Connection conn, String name, String location, String timeAndDate, int pointsNeeded, int pointsEarned) {
        try {
            String sql = "SELECT location FROM members WHERE location = ? AND timeAndDate = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, location);
            stmt.setString(2, timeAndDate);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            String sql = "INSERT INTO events VALUES (?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, location);
            stmt.setString(3, timeAndDate);
            stmt.setInt(4, pointsNeeded);
            stmt.setInt(5, pointsEarned);
            stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void editEvent(Connection conn, String name, String location, String timeAndDate, int pointsNeeded, int pointsEarned) {
        // TODO
    }

    public static void deleteEvent(Connection conn, String location, String timeAndDate) {
        try {
            String sql = "SELECT location FROM members WHERE location = ? AND timeAndDate = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, location);
            stmt.setString(2, timeAndDate);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) {
                return;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            String sql = "DELETE FROM events WHERE location = ? AND timeAndDate = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, location);
            stmt.setString(2, timeAndDate);
            stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}