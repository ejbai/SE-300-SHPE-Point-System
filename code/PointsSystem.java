import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.*;
import javax.swing.table.DefaultTableModel;

public class PointsSystem {

    public static ResultSet checkLogin(Connection conn, String username, String password) {
        try {
            String hashedPassword = hashPassword(password);
            String sql = "SELECT studentID, userRank FROM accounts WHERE username=? AND passwordHashSHA256=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, hashedPassword);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String hashPassword(String plaintextPassword) {
        try {
            // Get the SHA-256 algorithm instance
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Convert input string to bytes and compute the hash
            byte[] encodedHash = digest.digest(plaintextPassword.getBytes(StandardCharsets.UTF_8));

            // Convert byte array into a hex string
            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0'); // Ensure lead zero for single digits
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }


    public static ResultSet createUser(Connection conn, int studentID) {
        try {
            String sql = "SELECT * FROM members WHERE studentID=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentID);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet showUpcomingEvents(Connection conn) {
        try {
            String sql = """
                    SELECT name, location, timeAndDate, pointsNeeded, pointsEarned
                    FROM events
                    WHERE timeAndDate >= datetime('now')
                    ORDER BY timeAndDate ASC
                    """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet showAllEvents(Connection conn) {
        try {
            String sql = """
                    SELECT name, location, timeAndDate, pointsNeeded, pointsEarned
                    FROM events
                    ORDER BY timeAndDate ASC
                    """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet viewCurrentMemberNameAndPoints(Connection conn, int studentID) {
        try {
            String sql = "SELECT firstName, lastName, points FROM members WHERE studentID=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentID);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet viewAllMemberContacts(Connection conn) {
        try {
            String sql = """
                    SELECT firstName, lastName, email, phoneNumber
                    FROM members
                    ORDER BY lastName ASC, firstName ASC
                    """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet viewAllMembers(Connection conn) {
        try {
            String sql = """
                    SELECT studentID, firstName, lastName, email, phoneNumber, points
                    FROM members
                    ORDER BY lastName ASC, firstName ASC
                    """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet searchForMember(Connection conn, int studentID) {
        try {
            String sql = """
                    SELECT studentID, firstName, lastName, email, phoneNumber, points
                    FROM members
                    WHERE studentID=?
                    """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentID);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean addMember(Connection conn, int studentID, String firstName, String lastName, String email, String phoneNumber, int points) {
        try {
            String sql = """
                    INSERT INTO members (studentID, firstName, lastName, email, phoneNumber, points)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentID);
            stmt.setString(2, firstName);
            stmt.setString(3, lastName);
            stmt.setString(4, email);
            stmt.setString(5, phoneNumber);
            stmt.setInt(6, points);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean editMember(Connection conn, int studentID, String firstName, String lastName, String email, String phoneNumber, String pointsText) {
        try {
            String sql = """
                    UPDATE members
                    SET firstName = COALESCE(NULLIF(?, ''), firstName),
                        lastName = COALESCE(NULLIF(?, ''), lastName),
                        email = COALESCE(NULLIF(?, ''), email),
                        phoneNumber = COALESCE(NULLIF(?, ''), phoneNumber),
                        points = CASE
                                    WHEN NULLIF(?, '') IS NULL THEN points
                                    ELSE ?
                                 END
                    WHERE studentID = ?
                    """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, firstName.trim());
            stmt.setString(2, lastName.trim());
            stmt.setString(3, email.trim());
            stmt.setString(4, phoneNumber.trim());
            stmt.setString(5, pointsText.trim());

            if (pointsText.trim().isEmpty()) {
                stmt.setNull(6, Types.INTEGER);
            } else {
                stmt.setInt(6, Integer.parseInt(pointsText.trim()));
            }

            stmt.setInt(7, studentID);

            return stmt.executeUpdate() > 0;
        } catch (SQLException | NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteMember(Connection conn, int studentID) {
        try {
            String sql = "DELETE FROM members WHERE studentID=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean addEvent(Connection conn, String name, String location, String timeAndDate, int pointsNeeded, int pointsEarned) {
        try {
            String sql = """
                    INSERT INTO events (name, location, timeAndDate, pointsNeeded, pointsEarned)
                    VALUES (?, ?, ?, ?, ?)
                    """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, name);
            stmt.setString(2, location);
            stmt.setString(3, timeAndDate);
            stmt.setInt(4, pointsNeeded);
            stmt.setInt(5, pointsEarned);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean editEvent(Connection conn, String eventName, String location, String timeAndDate, int pointsNeeded, int pointsEarned) {
        try {
            String sql = """
                    UPDATE events
                    SET location=?, timeAndDate=?, pointsNeeded=?, pointsEarned=?
                    WHERE name=?
                    """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, location);
            stmt.setString(2, timeAndDate);
            stmt.setInt(3, pointsNeeded);
            stmt.setInt(4, pointsEarned);
            stmt.setString(5, eventName);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteEvent(Connection conn, String eventName) {
        try {
            String sql = "DELETE FROM events WHERE name=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, eventName);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean modifyMemberPoints(Connection conn, int studentID, int change) {
        try {
            String sql = "UPDATE members SET points = points + ? WHERE studentID=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, change);
            stmt.setInt(2, studentID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void loadMembersTable(DefaultTableModel model, Connection conn) {
        model.setRowCount(0);
        try {
            ResultSet rs = viewAllMembers(conn);
            while (rs != null && rs.next()) {
                model.addRow(new Object[]{rs.getInt("studentID"), rs.getString("firstName"), rs.getString("lastName"), rs.getString("email"), rs.getString("phoneNumber"), rs.getInt("points")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadSingleMemberTable(DefaultTableModel model, Connection conn, int studentID) {
        model.setRowCount(0);
        try {
            ResultSet rs = searchForMember(conn, studentID);
            while (rs != null && rs.next()) {
                model.addRow(new Object[]{rs.getInt("studentID"), rs.getString("firstName"), rs.getString("lastName"), rs.getString("email"), rs.getString("phoneNumber"), rs.getInt("points")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadEventsTable(DefaultTableModel model, Connection conn) {
        model.setRowCount(0);
        try {
            ResultSet rs = showAllEvents(conn);
            while (rs != null && rs.next()) {
                model.addRow(new Object[]{rs.getString("name"), rs.getString("location"), rs.getString("timeAndDate"), rs.getInt("pointsNeeded"), rs.getInt("pointsEarned")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadUpcomingEventsTable(DefaultTableModel model, Connection conn) {
        model.setRowCount(0);
        try {
            ResultSet rs = showUpcomingEvents(conn);
            while (rs != null && rs.next()) {
                model.addRow(new Object[]{rs.getString("name"), rs.getString("location"), rs.getString("timeAndDate"), rs.getInt("pointsNeeded"), rs.getInt("pointsEarned")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadPointsTable(DefaultTableModel model, Connection conn) {
        model.setRowCount(0);
        try {
            String sql = "SELECT studentID, firstName, lastName, points FROM members ORDER BY lastName ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{rs.getInt("studentID"), rs.getString("firstName"), rs.getString("lastName"), rs.getInt("points")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadContactsTable(DefaultTableModel model, Connection conn) {
        model.setRowCount(0);
        try {
            ResultSet rs = viewAllMemberContacts(conn);
            while (rs != null && rs.next()) {
                model.addRow(new Object[]{rs.getString("firstName"), rs.getString("lastName"), rs.getString("email"), rs.getString("phoneNumber")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        System.out.println(hashPassword("password"));
    }
}