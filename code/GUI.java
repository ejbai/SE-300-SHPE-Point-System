import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class GUI {

    public static void defaultGUI() {
        Connection conn = DatabaseConnection.connect();
        if (conn == null) return;

        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("SHPE Point Tracker");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 600);

            JPanel mainPanel = new JPanel(new BorderLayout());

            // LEFT PANEL
            JPanel leftPanel = new JPanel();
            leftPanel.setOpaque(true);
            leftPanel.setBackground(Color.WHITE);

            showDefaultLeftPanel(leftPanel, frame, conn);

            // RIGHT PANEL (LOGIN)
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));
            rightPanel.setBackground(new Color(245, 245, 245));

            JLabel loginTitle = new JLabel("Member Login");
            loginTitle.setFont(new Font("Arial", Font.BOLD, 30));
            loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel userLabel = new JLabel("Username:");
            JLabel passLabel = new JLabel("Password:");

            JTextField usernameField = new JTextField(15);
            JPasswordField passwordField = new JPasswordField(15);

            usernameField.setMaximumSize(usernameField.getPreferredSize());
            passwordField.setMaximumSize(passwordField.getPreferredSize());

            JButton loginButton = new JButton("Login");
            JLabel messageLabel = new JLabel(" ");

            loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
            userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
            passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);
            loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
            messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            rightPanel.add(Box.createVerticalGlue());
            rightPanel.add(loginTitle);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            rightPanel.add(userLabel);
            rightPanel.add(usernameField);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            rightPanel.add(passLabel);
            rightPanel.add(passwordField);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            rightPanel.add(loginButton);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            rightPanel.add(messageLabel);
            rightPanel.add(Box.createVerticalGlue());

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
            splitPane.setDividerLocation(750);

            mainPanel.add(splitPane, BorderLayout.CENTER);
            frame.setContentPane(mainPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            // LOGIN LOGIC
            loginButton.addActionListener(e -> {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                ResultSet rs = PointsSystem.checkLogin(conn, username, password);

                if (rs == null) {
                    messageLabel.setText("Invalid Credentials");
                    return;
                }

                try {
                    if (rs.next()) {
                        String userRank = rs.getString("userRank");
                        replaceLoginPanel(conn, rs, userRank, rightPanel);
                    } else {
                     messageLabel.setText("Invalid Credentials");
                    }
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            });
        });
    }

    // DEFAULT PAGE 
    public static void showDefaultLeftPanel(JPanel leftPanel, JFrame frame, Connection conn) {
        leftPanel.removeAll();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));

        JLabel label1 = new JLabel("View Upcoming Events");
        JButton button1 = new JButton("View");

        JLabel label2 = new JLabel("View Member Name and Points");
        JButton button2 = new JButton("View");

        label1.setAlignmentX(Component.CENTER_ALIGNMENT);
        button1.setAlignmentX(Component.CENTER_ALIGNMENT);
        label2.setAlignmentX(Component.CENTER_ALIGNMENT);
        button2.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(label1);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(button1);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        leftPanel.add(label2);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(button2);
        leftPanel.add(Box.createVerticalGlue());

        button1.addActionListener(e -> showEventsPanel(leftPanel, frame, conn));
        button2.addActionListener(e -> showMemberPointsPanel(leftPanel, frame, conn));

        leftPanel.revalidate();
        leftPanel.repaint();
    }

    // EVENTS PAGE
    public static void showEventsPanel(JPanel leftPanel, JFrame frame, Connection conn) {
        ResultSet rs = PointsSystem.showUpcomingEvents(conn);

        if (rs == null) {
            JOptionPane.showMessageDialog(frame, "No events found.");
            return;
        }

        String[] columns = {"Name", "Location", "Time and Date", "Points Needed", "Points Earned"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        try {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getString("timeAndDate"),
                        rs.getString("pointsNeeded"),
                        rs.getString("pointsEarned")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> showDefaultLeftPanel(leftPanel, frame, conn));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);

        leftPanel.removeAll();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(topPanel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        leftPanel.revalidate();
        leftPanel.repaint();
    }

    // MEMBER PAGE
    public static void showMemberPointsPanel(JPanel leftPanel, JFrame frame, Connection conn) {
        String input = JOptionPane.showInputDialog(frame, "Enter Student ID:");

        if (input == null || input.isEmpty()) return;

        int studentID;
        try {
            studentID = Integer.parseInt(input);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(frame, "Invalid ID");
            return;
        }

        ResultSet rs = PointsSystem.viewMemberNameAndPoints(conn, studentID);

        if (rs == null) {
            JOptionPane.showMessageDialog(frame, "No data found.");
            return;
        }

        String[] columns = {"Student ID", "First Name", "Last Name", "Points"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        try {
            while (rs.next()) {
                model.addRow(new Object[]{
                        studentID,
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("points")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        JButton backButton = new JButton("Back");
        backButton.addActionListener(e -> showDefaultLeftPanel(leftPanel, frame, conn));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);

        leftPanel.removeAll();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(topPanel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        leftPanel.revalidate();
        leftPanel.repaint();
    }

    // LOGIN REPLACEMENT 
    public static void replaceLoginPanel(Connection conn, ResultSet rs, String userRank, JPanel rightPanel) throws SQLException {
        ResultSet r = PointsSystem.createUser(conn, rs.getInt("studentID"));

        if (r == null || !r.next()) {
            JOptionPane.showMessageDialog(null, "User information not found.");
            return;
        }

        RegisteredUser user = new RegisteredUser(
                r.getInt("studentID"),
                r.getString("firstName"),
                r.getString("lastName"),
                r.getString("email"),
                r.getString("phoneNumber"),
                r.getInt("points"),
                userRank
        );

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));

        JLabel name = new JLabel(user.getFirstName() + " " + user.getLastName());
        JLabel rank = new JLabel(user.getRank());

        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        rank.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoPanel.add(Box.createVerticalGlue());
        infoPanel.add(name);
        infoPanel.add(rank);
        infoPanel.add(Box.createVerticalGlue());

        rightPanel.removeAll();
        rightPanel.setLayout(new BorderLayout());
        rightPanel.add(infoPanel, BorderLayout.CENTER);

        rightPanel.revalidate();
        rightPanel.repaint();
    }

    public static void main(String[] args) {
        defaultGUI();
    }
}