import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class SampleData {
    static String sample_events = """
            INSERT INTO events VALUES
                ('Event1', 'The park', '2026-04-02', 20, 5),
                ('Event2', 'The lake', '2026-04-06', 12, 3),
                ('Event3', 'The courtyard', '2026-04-07', 10, 7)
            ;
            """;

    public static void InsertData(Connection conn) {
        try (PreparedStatement stmt = conn.prepareStatement(sample_events)) {
            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
