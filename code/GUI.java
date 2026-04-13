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

                        if (userRank == null || userRank.trim().isEmpty()) {
                            messageLabel.setText("User rank is missing in accounts table");
                            return;
                        }

                        int studentID = rs.getInt("studentID");
                        ResultSet r = PointsSystem.createUser(conn, studentID);

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

                        switch (userRank) {
                            case "Member":
                                showMemberDashboard(leftPanel, frame, conn, user);
                                break;
                            case "Director":
                                showDirectorDashboard(leftPanel, frame, conn, user);
                                break;
                            case "Chairman":
                                showChairmanDashboard(leftPanel, frame, conn, user);
                                break;
                            default:
                                messageLabel.setText("Invalid Rank: " + userRank);
                                break;
                        }

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

        ResultSet rs = PointsSystem.viewCurrentMemberNameAndPoints(conn, studentID);

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
        infoPanel.add(logoutButton);
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
                    System.out.println("DEBUG userRank = " + userRank);

                    if (userRank == null || userRank.trim().isEmpty()) {
                        messageLabel.setText("User rank is missing in accounts table");
                        return;
                    }

                    int studentID = rs.getInt("studentID");
                    ResultSet r = PointsSystem.createUser(conn, studentID);

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

                    switch (userRank) {
                        case "Member":
                            showMemberDashboard(leftPanel, frame, conn, user);
                            break;
                        case "Director":
                            showDirectorDashboard(leftPanel, frame, conn, user);
                            break;
                        case "Chairman":
                            showChairmanDashboard(leftPanel, frame, conn, user);
                            break;
                        default:
                            messageLabel.setText("Invalid Rank: " + userRank);
                            break;
                    }
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

    
        public static void showDirectorDashboard(JPanel leftPanel, JFrame frame, Connection conn, RegisteredUser user) {
        leftPanel.removeAll();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        leftPanel.setBackground(Color.WHITE);

        JLabel welcomeLabel = new JLabel("Director Dashboard");
        JLabel nameLabel = new JLabel(user.getFirstName() + " " + user.getLastName());

        JButton membersButton = new JButton("Manage Members");
        JButton eventsButton = new JButton("Manage Events");
        JButton pointsButton = new JButton("View All Member Points");

        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        membersButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        eventsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pointsButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalGlue());
            leftPanel.add(welcomeLabel);
            leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            leftPanel.add(nameLabel);
            leftPanel.add(Box.createRigidArea(new Dimension(0, 30)));
            leftPanel.add(membersButton);
            leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            leftPanel.add(eventsButton);
            leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
            leftPanel.add(pointsButton);
            leftPanel.add(Box.createVerticalGlue());

            membersButton.addActionListener(e -> showDirectorMembersPanel(leftPanel, frame, conn, user));
            eventsButton.addActionListener(e -> showDirectorEventsPanel(leftPanel, frame, conn, user));
            pointsButton.addActionListener(e -> showDirectorPointsPanel(leftPanel, frame, conn, user));

            leftPanel.revalidate();
            leftPanel.repaint();
    }
    public static void showDirectorMembersPanel(JPanel leftPanel, JFrame frame, Connection conn, RegisteredUser user) {
    leftPanel.removeAll();
    leftPanel.setLayout(new BorderLayout());
    leftPanel.setBackground(Color.WHITE);

    String[] columns = {"Student ID", "First Name", "Last Name", "Email", "Phone Number", "Points"};
    DefaultTableModel model = new DefaultTableModel(columns, 0);
    JTable table = new JTable(model);
    JScrollPane scrollPane = new JScrollPane(table);

    loadAllMembers(model, conn);

    JButton backButton = new JButton("Back");
    JButton refreshButton = new JButton("Refresh");
    JButton searchButton = new JButton("Search Member");
    JButton addButton = new JButton("Add Member");
    JButton editButton = new JButton("Edit Member");
    JButton deleteButton = new JButton("Delete Member");

    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    topPanel.add(backButton);
    topPanel.add(refreshButton);
    topPanel.add(searchButton);
    topPanel.add(addButton);
    topPanel.add(editButton);
    topPanel.add(deleteButton);

    backButton.addActionListener(e -> showDirectorDashboard(leftPanel, frame, conn, user));

    refreshButton.addActionListener(e -> loadAllMembers(model, conn));

    searchButton.addActionListener(e -> {
        String input = JOptionPane.showInputDialog(frame, "Enter Student ID:");
        if (input == null || input.trim().isEmpty()) return;

        try {
            int studentID = Integer.parseInt(input.trim());
            loadSingleMember(model, conn, studentID);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(frame, "Invalid Student ID.");
        }
    });

    addButton.addActionListener(e -> {
        JTextField idField = new JTextField();
        JTextField firstField = new JTextField();
        JTextField lastField = new JTextField();
        JTextField emailField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField pointsField = new JTextField();

        Object[] fields = {
                "Student ID:", idField,
                "First Name:", firstField,
                "Last Name:", lastField,
                "Email:", emailField,
                "Phone Number:", phoneField,
                "Points:", pointsField
        };

        int result = JOptionPane.showConfirmDialog(frame, fields, "Add Member", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                int studentID = Integer.parseInt(idField.getText().trim());
                String firstName = firstField.getText().trim();
                String lastName = lastField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();
                int points = Integer.parseInt(pointsField.getText().trim());

                String sql = "INSERT INTO members (studentID, firstName, lastName, email, phoneNumber, points) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, studentID);
                stmt.setString(2, firstName);
                stmt.setString(3, lastName);
                stmt.setString(4, email);
                stmt.setString(5, phone);
                stmt.setInt(6, points);
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Member added successfully.");
                loadAllMembers(model, conn);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error adding member.");
            }
        }
    });

    editButton.addActionListener(e -> {
        String input = JOptionPane.showInputDialog(frame, "Enter Student ID of member to edit:");
        if (input == null || input.trim().isEmpty()) return;

        try {
            int studentID = Integer.parseInt(input.trim());

            JTextField firstField = new JTextField();
            JTextField lastField = new JTextField();
            JTextField emailField = new JTextField();
            JTextField phoneField = new JTextField();
            JTextField pointsField = new JTextField();

            Object[] fields = {
                    "First Name:", firstField,
                    "Last Name:", lastField,
                    "Email:", emailField,
                    "Phone Number:", phoneField,
                    "Points:", pointsField
            };

            int result = JOptionPane.showConfirmDialog(frame, fields, "Edit Member", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String sql = "UPDATE members SET firstName=?, lastName=?, email=?, phoneNumber=?, points=? WHERE studentID=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, firstField.getText().trim());
                stmt.setString(2, lastField.getText().trim());
                stmt.setString(3, emailField.getText().trim());
                stmt.setString(4, phoneField.getText().trim());
                stmt.setInt(5, Integer.parseInt(pointsField.getText().trim()));
                stmt.setInt(6, studentID);

                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    JOptionPane.showMessageDialog(frame, "Member updated successfully.");
                    loadAllMembers(model, conn);
                } else {
                    JOptionPane.showMessageDialog(frame, "Member not found.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error editing member.");
        }
    });

    deleteButton.addActionListener(e -> {
        String input = JOptionPane.showInputDialog(frame, "Enter Student ID of member to delete:");
        if (input == null || input.trim().isEmpty()) return;

        try {
            int studentID = Integer.parseInt(input.trim());

            int confirm = JOptionPane.showConfirmDialog(
                    frame,
                    "Delete member with ID " + studentID + "?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                String sql = "DELETE FROM members WHERE studentID=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, studentID);

                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    JOptionPane.showMessageDialog(frame, "Member deleted successfully.");
                    loadAllMembers(model, conn);
                } else {
                    JOptionPane.showMessageDialog(frame, "Member not found.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Error deleting member.");
        }
    });

    leftPanel.add(topPanel, BorderLayout.NORTH);
    leftPanel.add(scrollPane, BorderLayout.CENTER);

    leftPanel.revalidate();
    leftPanel.repaint();
}

public static void showDirectorEventsPanel(JPanel leftPanel, JFrame frame, Connection conn, RegisteredUser user) {
    leftPanel.removeAll();
    leftPanel.setLayout(new BorderLayout());
    leftPanel.setBackground(Color.WHITE);

    String[] columns = {"Name", "Location", "Time and Date", "Points Needed", "Points Earned"};
    DefaultTableModel model = new DefaultTableModel(columns, 0);
    JTable table = new JTable(model);
    JScrollPane scrollPane = new JScrollPane(table);

    loadAllEvents(model, conn);

    JButton backButton = new JButton("Back");
    JButton refreshButton = new JButton("Refresh");
    JButton addButton = new JButton("Add Event");
    JButton editButton = new JButton("Edit Event");

    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    topPanel.add(backButton);
    topPanel.add(refreshButton);
    topPanel.add(addButton);
    topPanel.add(editButton);

    backButton.addActionListener(e -> showDirectorDashboard(leftPanel, frame, conn, user));
    refreshButton.addActionListener(e -> loadAllEvents(model, conn));

    addButton.addActionListener(e -> {
        JTextField nameField = new JTextField();
        JTextField locationField = new JTextField();
        JTextField timeField = new JTextField();
        JTextField pointsNeededField = new JTextField();
        JTextField pointsEarnedField = new JTextField();

        Object[] fields = {
                "Name:", nameField,
                "Location:", locationField,
                "Time and Date:", timeField,
                "Points Needed:", pointsNeededField,
                "Points Earned:", pointsEarnedField
        };

        int result = JOptionPane.showConfirmDialog(frame, fields, "Add Event", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String sql = "INSERT INTO events (name, location, timeAndDate, pointsNeeded, pointsEarned) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, nameField.getText().trim());
                stmt.setString(2, locationField.getText().trim());
                stmt.setString(3, timeField.getText().trim());
                stmt.setInt(4, Integer.parseInt(pointsNeededField.getText().trim()));
                stmt.setInt(5, Integer.parseInt(pointsEarnedField.getText().trim()));
                stmt.executeUpdate();

                JOptionPane.showMessageDialog(frame, "Event added successfully.");
                loadAllEvents(model, conn);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error adding event.");
            }
        }
    });

    editButton.addActionListener(e -> {
        String eventName = JOptionPane.showInputDialog(frame, "Enter event name to edit:");
        if (eventName == null || eventName.trim().isEmpty()) return;

        JTextField locationField = new JTextField();
        JTextField timeField = new JTextField();
        JTextField pointsNeededField = new JTextField();
        JTextField pointsEarnedField = new JTextField();

        Object[] fields = {
                "New Location:", locationField,
                "New Time and Date:", timeField,
                "New Points Needed:", pointsNeededField,
                "New Points Earned:", pointsEarnedField
        };

        int result = JOptionPane.showConfirmDialog(frame, fields, "Edit Event", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String sql = "UPDATE events SET location=?, timeAndDate=?, pointsNeeded=?, pointsEarned=? WHERE name=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, locationField.getText().trim());
                stmt.setString(2, timeField.getText().trim());
                stmt.setInt(3, Integer.parseInt(pointsNeededField.getText().trim()));
                stmt.setInt(4, Integer.parseInt(pointsEarnedField.getText().trim()));
                stmt.setString(5, eventName.trim());

                int rows = stmt.executeUpdate();

                if (rows > 0) {
                    JOptionPane.showMessageDialog(frame, "Event updated successfully.");
                    loadAllEvents(model, conn);
                } else {
                    JOptionPane.showMessageDialog(frame, "Event not found.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(frame, "Error editing event.");
            }
        }
    });

    leftPanel.add(topPanel, BorderLayout.NORTH);
    leftPanel.add(scrollPane, BorderLayout.CENTER);

    leftPanel.revalidate();
    leftPanel.repaint();
}

public static void showDirectorPointsPanel(JPanel leftPanel, JFrame frame, Connection conn, RegisteredUser user) {
    leftPanel.removeAll();
    leftPanel.setLayout(new BorderLayout());
    leftPanel.setBackground(Color.WHITE);

    String[] columns = {"Student ID", "First Name", "Last Name", "Points"};
    DefaultTableModel model = new DefaultTableModel(columns, 0);

    try {
        String sql = "SELECT studentID, firstName, lastName, points FROM members ORDER BY lastName ASC";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();

        while (rs.next()) {
            model.addRow(new Object[]{
                    rs.getInt("studentID"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getInt("points")
            });
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(frame, "Error loading member points.");
        return;
    }

    JTable table = new JTable(model);
    JScrollPane scrollPane = new JScrollPane(table);

    JButton backButton = new JButton("Back");
    backButton.addActionListener(e -> showDirectorDashboard(leftPanel, frame, conn, user));

    JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    topPanel.add(backButton);

    leftPanel.add(topPanel, BorderLayout.NORTH);
    leftPanel.add(scrollPane, BorderLayout.CENTER);

    leftPanel.revalidate();
    leftPanel.repaint();
}

    public static void showChairmanDashboard(JPanel leftPanel, JFrame frame, Connection conn, RegisteredUser user) {
        // TODO
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

    public static void loadAllMembers(DefaultTableModel model, Connection conn) {
        model.setRowCount(0);

        try {
            String sql = "SELECT studentID, firstName, lastName, email, phoneNumber, points FROM members ORDER BY lastName ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("studentID"),
                    rs.getString("firstName"),
                    rs.getString("lastName"),
                    rs.getString("email"),
                    rs.getString("phoneNumber"),
                    rs.getInt("points")
                });
            }
        } catch (SQLException ex) {
        ex.printStackTrace();
        }
    }

    public static void loadSingleMember(DefaultTableModel model, Connection conn, int studentID) {
        model.setRowCount(0);

        try {
            String sql = "SELECT studentID, firstName, lastName, email, phoneNumber, points FROM members WHERE studentID=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, studentID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("studentID"),
                        rs.getString("firstName"),
                        rs.getString("lastName"),
                        rs.getString("email"),
                        rs.getString("phoneNumber"),
                        rs.getInt("points")
                });
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    public static void loadAllEvents(DefaultTableModel model, Connection conn) {
        model.setRowCount(0);

        try {
            String sql = "SELECT name, location, timeAndDate, pointsNeeded, pointsEarned FROM events ORDER BY timeAndDate ASC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getString("location"),
                    rs.getString("timeAndDate"),
                    rs.getInt("pointsNeeded"),
                    rs.getInt("pointsEarned")
                });
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }
}

    public static void main(String[] args) {
        defaultGUI();
    }
}