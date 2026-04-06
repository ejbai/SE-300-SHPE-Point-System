import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SetUpDB {
    public static void createTables(Connection conn) throws SQLException {
        String events = """
                CREATE TABLE IF NOT EXISTS events (
                    name VARCHAR(256) NOT NULL,
                    location VARCHAR(256) NOT NULL,
                    timeAndDate DATETIME NOT NULL,
                    pointsNeeded INT,
                    pointsEarned INT,
                    PRIMARY KEY (location, timeAndDate)
                );
                """;

        String members = """
                CREATE TABLE IF NOT EXISTS members (
                    studentID INT NOT NULL,
                    firstName VARCHAR(256) NOT NULL,
                    lastName VARCHAR(256) NOT NULL,
                    email VARCHAR(256),
                    phoneNumber VARCHAR(256),
                    points INT,
                    PRIMARY KEY (studentID)
                );
                """;

        String accounts = """
                CREATE TABLE IF NOT EXISTS accounts (
                    username VARCHAR(256) NOT NULL,
                    passwordHashSHA256 VARCHAR(512) NOT NULL,
                    studentID INT NOT NULL,
                    userRank CHECK (userRank IN ('Member', 'Director', 'Chairman')) NOT NULL,
                    PRIMARY KEY (username)
                );
                """;

        // Add events table
        PreparedStatement add_events = conn.prepareStatement(events);
        add_events.execute();

        // Add members table
        PreparedStatement add_members = conn.prepareStatement(members);
        add_members.execute();

        // Add accounts table
        PreparedStatement add_accounts = conn.prepareStatement(accounts);
        add_accounts.execute();
    }

    public static void main(String[] args) throws SQLException {
        Connection conn = DatabaseConnection.connect();
        if  (conn == null) {
            System.out.println("Unable to establish connection");
            return;
        }

        createTables(conn);
        SampleData.InsertData(conn);
    }
}
