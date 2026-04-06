import javax.swing.*;
import java.awt.*;
import javax.swing.table.DefaultTableModel;
import java.sql.*;

// TODO:
//  make different views for each of the four users based on login, and buttons that do things in each of them such as show a list of things or ask for inputs
//  ...

public class GUI {
    public static void defaultGUI() {
        Connection conn = DatabaseConnection.connect();
        if (conn == null) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("SHPE Point Tracker");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 600);
            JPanel mainPanel = new JPanel(new BorderLayout());

            //  Left panel
            JPanel leftPanel = new JPanel();
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
            leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));
            leftPanel.setOpaque(true);
            leftPanel.setBackground(Color.WHITE);

            // Upcoming events button on default view
            JLabel label = new JLabel("View Upcoming Events");
            JButton button = new JButton("View");

            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);

            leftPanel.add(Box.createVerticalGlue());
            leftPanel.add(label);
            leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            leftPanel.add(button);
            leftPanel.add(Box.createVerticalGlue());

            // View upcoming events button

            button.addActionListener(e -> {
                ResultSet rs = PointsSystem.showUpcomingEvents(conn);

                String[] columns = {"Name", "Location", "Time and Date", "Points Needed", "Points Earned"};

                DefaultTableModel model = new DefaultTableModel(columns, 0);

                try {
                    do {
                        String name = rs.getString("name");
                        String location = rs.getString("location");
                        String date = rs.getString("timeAndDate");
                        String pointsNeeded = rs.getString("pointsNeeded");
                        String pointsEarned = rs.getString("pointsEarned");

                        model.addRow(new Object[]{name, location, date, pointsNeeded, pointsEarned});
                    } while (rs.next());
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

                JTable table = new JTable(model);

                JScrollPane scrollPane = new JScrollPane(table);

                leftPanel.removeAll();
                leftPanel.setLayout(new BorderLayout());
                leftPanel.add(scrollPane, BorderLayout.CENTER);

                leftPanel.revalidate();
                leftPanel.repaint();
            });

            // View member name and points button on member view
            JLabel label2 = new JLabel("View Member Name and Points");
            JButton button2 = new JButton("View");

            label2.setAlignmentX(Component.CENTER_ALIGNMENT);
            button2.setAlignmentX(Component.CENTER_ALIGNMENT);

            leftPanel.add(Box.createVerticalGlue());
            leftPanel.add(label2);
            leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            leftPanel.add(button2);
            leftPanel.add(Box.createVerticalGlue());

            // View member name and points button

            button2.addActionListener(e -> {
                // Prompt for student ID input
                String studentIDString = JOptionPane.showInputDialog(
                        frame,
                        "Enter Student ID:",
                        "Student ID Lookup",
                        JOptionPane.PLAIN_MESSAGE
                );

                // If user canceled or left it blank, do nothing
                if (studentIDString == null || studentIDString.trim().isEmpty()) {
                    return;
                }
                int studentID = Integer.parseInt(studentIDString);

                // Continue with button
                ResultSet rs = PointsSystem.viewMemberNameAndPoints(conn, studentID);

                String[] columns = {"Student ID", "First Name", "Last Name", "Points"};

                DefaultTableModel model = new DefaultTableModel(columns, 0);

                try {
                    do {
                        String firstName = rs.getString("firstName");
                        String lastName = rs.getString("lastName");
                        String points = rs.getString("points");

                        model.addRow(new Object[]{studentID, firstName, lastName, points});
                    } while (rs.next());
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

                JTable table = new JTable(model);

                JScrollPane scrollPane = new JScrollPane(table);

                leftPanel.removeAll();
                leftPanel.setLayout(new BorderLayout());
                leftPanel.add(scrollPane, BorderLayout.CENTER);

                leftPanel.revalidate();
                leftPanel.repaint();
            });

            //  Right panel (login)
            JPanel rightPanel = new JPanel();
            rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
            rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 50));

            rightPanel.setOpaque(true);
            rightPanel.setBackground(new Color(245, 245, 245));

            JLabel loginTitle = new JLabel("Member Login");
            loginTitle.setFont(new Font("Arial", Font.BOLD, 30));
            loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel userLabel = new JLabel("Username:");
            JLabel passLabel = new JLabel("Password:");
            userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            passLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JTextField usernameField = new JTextField(15);
            JPasswordField passwordField = new JPasswordField(15);

            usernameField.setAlignmentX(Component.CENTER_ALIGNMENT);
            passwordField.setAlignmentX(Component.CENTER_ALIGNMENT);

            usernameField.setHorizontalAlignment(JTextField.CENTER);
            passwordField.setHorizontalAlignment(JTextField.CENTER);

            usernameField.setMaximumSize(usernameField.getPreferredSize());
            passwordField.setMaximumSize(passwordField.getPreferredSize());

            JButton loginButton = new JButton("Login");
            loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel messageLabel = new JLabel(" ");
            messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            // Login

            loginButton.addActionListener(e -> {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

//                String hashedPassword = hashPassword(password); // TODO: the hashing isn't working
                String rank = PointsSystem.checkLogin(conn, username, password);    // TODO: create a new menu on the left based on the login rank

                if (rank != null) {

                    RegisteredUser user = new RegisteredUser();

                    messageLabel.setText("Login Successful (" + rank + ")");

                } else {

                    messageLabel.setText("Invalid Credentials");

                }

            });

            rightPanel.add(Box.createVerticalGlue());
            rightPanel.add(loginTitle);

            rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            rightPanel.add(userLabel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            rightPanel.add(usernameField);

            rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            rightPanel.add(passLabel);
            rightPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            rightPanel.add(passwordField);

            rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            rightPanel.add(loginButton);

            rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            rightPanel.add(messageLabel);
            rightPanel.add(Box.createVerticalGlue());

            //  Divider between left and right panes
            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);

            splitPane.setDividerLocation(750);
            splitPane.setDividerSize(8);
            splitPane.setContinuousLayout(true);
            splitPane.setBorder(BorderFactory.createEmptyBorder());

            splitPane.setEnabled(true);
            splitPane.setOneTouchExpandable(false);

            mainPanel.add(splitPane, BorderLayout.CENTER);

            frame.setContentPane(mainPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public static void main(String[] args) {
        defaultGUI();
    }
}
