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
            ResultSet r = PointsSystem.createUser(conn, rs.getInt("studentID"));

            if (r == null || !r.next()) {
                messageLabel.setText("User information not found.");
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

            replaceLoginPanel(user, rightPanel, leftPanel, frame, conn);
            showMemberDashboard(leftPanel, frame, conn, user);

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


        label1.setAlignmentX(Component.CENTER_ALIGNMENT);
        button1.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(label1);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(button1);
        leftPanel.add(Box.createVerticalGlue());

        button1.addActionListener(e -> showEventsPanel(leftPanel, frame, conn));

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
    public static void replaceLoginPanel(RegisteredUser user, JPanel rightPanel, JPanel leftPanel, JFrame frame, Connection conn) {

    JPanel infoPanel = new JPanel();
    infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
    infoPanel.setBackground(new Color(245, 245, 245));

    JLabel welcomeLabel = new JLabel("Welcome,");
    JLabel nameLabel = new JLabel(user.getFirstName() + " " + user.getLastName());
    JLabel rankLabel = new JLabel(user.getRank());

    JButton logoutButton = new JButton("Logout");

    welcomeLabel.setFont(new Font("Arial", Font.PLAIN, 18));
    nameLabel.setFont(new Font("Arial", Font.BOLD, 24));
    rankLabel.setFont(new Font("Arial", Font.PLAIN, 16));

    welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    rankLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    logoutButton.setAlignmentX(Component.CENTER_ALIGNMENT);

    infoPanel.add(Box.createVerticalGlue());
    infoPanel.add(welcomeLabel);
    infoPanel.add(Box.createRigidArea(new Dimension(0, 5)));
    infoPanel.add(nameLabel);
    infoPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    infoPanel.add(rankLabel);
    infoPanel.add(Box.createRigidArea(new Dimension(0, 20)));
    infoPanel.add(logoutButton);   // 👈 HERE
    infoPanel.add(Box.createVerticalGlue());

    rightPanel.removeAll();
    rightPanel.setLayout(new BorderLayout());
    rightPanel.add(infoPanel, BorderLayout.CENTER);

    rightPanel.revalidate();
    rightPanel.repaint();

    
    logoutButton.addActionListener(e -> {
        
        showDefaultLeftPanel(leftPanel, frame, conn);
        showLoginPanel(rightPanel, frame, conn, leftPanel);
    });
    }

    public static void showLoginPanel(JPanel rightPanel, JFrame frame, Connection conn, JPanel leftPanel) {

    rightPanel.removeAll();
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

    rightPanel.revalidate();
    rightPanel.repaint();

    // SHOW LOGIN PAGE AGAIN
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
                ResultSet r = PointsSystem.createUser(conn, rs.getInt("studentID"));

                if (r == null || !r.next()) {
                    messageLabel.setText("User info not found");
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

                replaceLoginPanel(user, rightPanel, leftPanel, frame, conn);
                showMemberDashboard(leftPanel, frame, conn, user);

            } else {
                messageLabel.setText("Invalid Credentials");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    });
    }

    // REPLACEMENT FOR MEMBER LOGIN
    public static void showMemberDashboard(JPanel leftPanel, JFrame frame, Connection conn, RegisteredUser user) {
    leftPanel.removeAll();
    leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
    leftPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
    leftPanel.setBackground(Color.WHITE);

    JLabel welcomeLabel = new JLabel("Welcome, " + user.getFirstName() + " " + user.getLastName());
    JLabel pointsLabel = new JLabel("Points: " + user.getPoints());

    JButton eventsButton = new JButton("View Upcoming Events");
    JButton contactsButton = new JButton("View Club Contacts");

    welcomeLabel.setFont(new Font("Arial", Font.BOLD, 22));
    pointsLabel.setFont(new Font("Arial", Font.PLAIN, 18));

    welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    pointsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
    eventsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
    contactsButton.setAlignmentX(Component.CENTER_ALIGNMENT);

    leftPanel.add(Box.createVerticalGlue());
    leftPanel.add(welcomeLabel);
    leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    leftPanel.add(pointsLabel);
    leftPanel.add(Box.createRigidArea(new Dimension(0, 30)));
    leftPanel.add(eventsButton);
    leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    leftPanel.add(contactsButton);
    leftPanel.add(Box.createVerticalGlue());

    eventsButton.addActionListener(e -> showMemberEventsPanel(leftPanel, frame, conn, user));
    contactsButton.addActionListener(e -> showClubContactsPanel(leftPanel, frame, conn, user));

    leftPanel.revalidate();
    leftPanel.repaint();
    }

    // SHOW CLUB CONTACTS
    public static void showClubContactsPanel(JPanel leftPanel, JFrame frame, Connection conn, RegisteredUser user) {
    ResultSet rs = PointsSystem.viewAllMemberContacts(conn);

    if (rs == null) {
        JOptionPane.showMessageDialog(frame, "No member contacts found.");
        return;
    }

    String[] columns = {"First Name", "Last Name", "Email", "Phone Number"};
    DefaultTableModel model = new DefaultTableModel(columns, 0);

    try {
        boolean found = false;

        while (rs.next()) {
            found = true;
            model.addRow(new Object[]{
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("email"),
                    rs.getString("phoneNumber")
            });
        }

        if (!found) {
            JOptionPane.showMessageDialog(frame, "No member contacts found.");
            return;
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(frame, "Error loading contacts.");
        return;
    }

    JTable table = new JTable(model);
    JScrollPane scrollPane = new JScrollPane(table);

    JButton backButton = new JButton("Back");
    backButton.addActionListener(e -> showMemberDashboard(leftPanel, frame, conn, user));

    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    topPanel.add(backButton);

    leftPanel.removeAll();
    leftPanel.setLayout(new BorderLayout());
    leftPanel.add(topPanel, BorderLayout.NORTH);
    leftPanel.add(scrollPane, BorderLayout.CENTER);

    leftPanel.revalidate();
    leftPanel.repaint();
    } 

    // SHOW MEMBER EVENTS
    public static void showMemberEventsPanel(JPanel leftPanel, JFrame frame, Connection conn, RegisteredUser user) {
    ResultSet rs = PointsSystem.showUpcomingEvents(conn);

    if (rs == null) {
        JOptionPane.showMessageDialog(frame, "No events found.");
        return;
    }

    String[] columns = {"Name", "Location", "Time and Date", "Points Needed", "Points Earned"};
    DefaultTableModel model = new DefaultTableModel(columns, 0);

    try {
        boolean found = false;

        while (rs.next()) {
            found = true;
            model.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getString("location"),
                    rs.getString("timeAndDate"),
                    rs.getString("pointsNeeded"),
                    rs.getString("pointsEarned")
            });
        }

        if (!found) {
            JOptionPane.showMessageDialog(frame, "No events found.");
            return;
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(frame, "Error loading events.");
        return;
    }

    JTable table = new JTable(model);
    JScrollPane scrollPane = new JScrollPane(table);

    JButton backButton = new JButton("Back");
    backButton.addActionListener(e -> showMemberDashboard(leftPanel, frame, conn, user));

    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    topPanel.add(backButton);

    leftPanel.removeAll();
    leftPanel.setLayout(new BorderLayout());
    leftPanel.add(topPanel, BorderLayout.NORTH);
    leftPanel.add(scrollPane, BorderLayout.CENTER);

    leftPanel.revalidate();
    leftPanel.repaint();
    }

    public static void main(String[] args) {
        defaultGUI();
    }
}