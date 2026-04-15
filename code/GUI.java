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

            JPanel leftPanel = new JPanel();
            leftPanel.setOpaque(true);
            leftPanel.setBackground(Color.WHITE);
            showDefaultLeftPanel(leftPanel, frame, conn);

            JPanel rightPanel = new JPanel();
            buildLoginPanel(rightPanel, frame, conn, leftPanel);

            JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
            splitPane.setDividerLocation(750);

            mainPanel.add(splitPane, BorderLayout.CENTER);
            frame.setContentPane(mainPanel);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    public static void buildLoginPanel(JPanel rightPanel, JFrame frame, Connection conn, JPanel leftPanel) {
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

        loginButton.addActionListener(e ->
                handleLogin(conn, usernameField, passwordField, messageLabel, rightPanel, leftPanel, frame)
        );

        rightPanel.revalidate();
        rightPanel.repaint();
    }

    private static void handleLogin(Connection conn, JTextField usernameField, JPasswordField passwordField,
                                    JLabel messageLabel, JPanel rightPanel, JPanel leftPanel, JFrame frame) {
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

                String normalizedRank = userRank.trim().toLowerCase();
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
                        r.getInt("points"),
                        normalizedRank
                );

                replaceLoginPanel(user, rightPanel, leftPanel, frame, conn);

                switch (normalizedRank) {
                    case "member":
                        showMemberDashboard(leftPanel, frame, conn, user);
                        break;
                    case "director":
                        showDirectorDashboard(leftPanel, frame, conn, user);
                        break;
                    case "chairman":
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
            messageLabel.setText("Login error.");
        }
    }

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

        button1.addActionListener(e -> showPublicEventsPanel(leftPanel, frame, conn));

        leftPanel.revalidate();
        leftPanel.repaint();
    }

    public static void showPublicEventsPanel(JPanel leftPanel, JFrame frame, Connection conn) {
        String[] columns = {"Name", "Location", "Time and Date", "Points Needed", "Points Earned"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        PointsSystem.loadUpcomingEventsTable(model, conn);

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(frame, "No events found.");
            return;
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
            buildLoginPanel(rightPanel, frame, conn, leftPanel);
        });
    }

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
        JLabel pointsLabel = new JLabel("Points: " + user.getPoints());

        JButton membersButton = new JButton("Manage Members");
        JButton eventsButton = new JButton("Manage Events");
        JButton pointsButton = new JButton("View All Member Points");

        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        nameLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        pointsLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        pointsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        membersButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        eventsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pointsButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(welcomeLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(nameLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(pointsLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        leftPanel.add(membersButton);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(eventsButton);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(pointsButton);
        leftPanel.add(Box.createVerticalGlue());

        membersButton.addActionListener(e -> showManageMembersPanel(leftPanel, frame, conn, user, "director"));
        eventsButton.addActionListener(e -> showManageEventsPanel(leftPanel, frame, conn, user, "director"));
        pointsButton.addActionListener(e -> showDirectorPointsPanel(leftPanel, frame, conn, user));

        leftPanel.revalidate();
        leftPanel.repaint();
    }

    public static void showChairmanDashboard(JPanel leftPanel, JFrame frame, Connection conn, RegisteredUser user) {
        leftPanel.removeAll();
        leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
        leftPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        leftPanel.setBackground(Color.WHITE);

        JLabel title = new JLabel("Chairman Dashboard");
        JLabel name = new JLabel(user.getFirstName() + " " + user.getLastName());
        JLabel pointsLabel = new JLabel("Points: " + user.getPoints());

        JButton membersButton = new JButton("Manage Members");
        JButton eventsButton = new JButton("Manage Events");
        JButton pointsButton = new JButton("Modify Member Points");

        title.setFont(new Font("Arial", Font.BOLD, 24));
        name.setFont(new Font("Arial", Font.PLAIN, 18));
        pointsLabel.setFont(new Font("Arial", Font.PLAIN, 18));

        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        name.setAlignmentX(Component.CENTER_ALIGNMENT);
        pointsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        membersButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        eventsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        pointsButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        leftPanel.add(Box.createVerticalGlue());
        leftPanel.add(title);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        leftPanel.add(name);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(pointsLabel);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 30)));
        leftPanel.add(membersButton);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(eventsButton);
        leftPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        leftPanel.add(pointsButton);
        leftPanel.add(Box.createVerticalGlue());

        membersButton.addActionListener(e -> showManageMembersPanel(leftPanel, frame, conn, user, "chairman"));
        eventsButton.addActionListener(e -> showManageEventsPanel(leftPanel, frame, conn, user, "chairman"));
        pointsButton.addActionListener(e -> showModifyPointsPanel(leftPanel, frame, conn, user));

        leftPanel.revalidate();
        leftPanel.repaint();
    }

    public static void showClubContactsPanel(JPanel leftPanel, JFrame frame, Connection conn, RegisteredUser user) {
        String[] columns = {"First Name", "Last Name", "Email", "Phone Number"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        PointsSystem.loadContactsTable(model, conn);

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(frame, "No member contacts found.");
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

    public static void showMemberEventsPanel(JPanel leftPanel, JFrame frame, Connection conn, RegisteredUser user) {
        String[] columns = {"Name", "Location", "Time and Date", "Points Needed", "Points Earned"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        PointsSystem.loadUpcomingEventsTable(model, conn);

        if (model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(frame, "No events found.");
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

    public static void showManageMembersPanel(JPanel leftPanel, JFrame frame, Connection conn, RegisteredUser user, String role) {
        leftPanel.removeAll();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);

        String[] columns = {"Student ID", "First Name", "Last Name", "Email", "Phone Number", "Points"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        PointsSystem.loadMembersTable(model, conn);

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

        backButton.addActionListener(e -> {
            if (role.equals("chairman")) {
                showChairmanDashboard(leftPanel, frame, conn, user);
            } else {
                showDirectorDashboard(leftPanel, frame, conn, user);
            }
        });

        refreshButton.addActionListener(e -> PointsSystem.loadMembersTable(model, conn));

        searchButton.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(frame, "Enter Student ID:");
            if (input == null || input.trim().isEmpty()) return;

            try {
                int studentID = Integer.parseInt(input.trim());
                PointsSystem.loadSingleMemberTable(model, conn, studentID);
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
                    boolean success = PointsSystem.addMember(
                            conn,
                            Integer.parseInt(idField.getText().trim()),
                            firstField.getText().trim(),
                            lastField.getText().trim(),
                            emailField.getText().trim(),
                            phoneField.getText().trim(),
                            Integer.parseInt(pointsField.getText().trim())
                    );

                    if (success) {
                        JOptionPane.showMessageDialog(frame, "Member added successfully.");
                        PointsSystem.loadMembersTable(model, conn);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Error adding member.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input.");
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
                if (result != JOptionPane.OK_OPTION) return;

                boolean success = PointsSystem.editMember(
                        conn,
                        studentID,
                        firstField.getText(),
                        lastField.getText(),
                        emailField.getText(),
                        phoneField.getText(),
                        pointsField.getText()
                );

                if (success) {
                    JOptionPane.showMessageDialog(frame, "Member updated successfully.");
                    PointsSystem.loadMembersTable(model, conn);
                } else {
                    JOptionPane.showMessageDialog(frame, "Member not found or invalid input.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input.");
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
                    boolean success = PointsSystem.deleteMember(conn, studentID);

                    if (success) {
                        JOptionPane.showMessageDialog(frame, "Member deleted successfully.");
                        PointsSystem.loadMembersTable(model, conn);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Member not found.");
                    }
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame, "Invalid input.");
            }
        });

        leftPanel.add(topPanel, BorderLayout.NORTH);
        leftPanel.add(scrollPane, BorderLayout.CENTER);

        leftPanel.revalidate();
        leftPanel.repaint();
    }

    public static void showManageEventsPanel(JPanel leftPanel, JFrame frame, Connection conn, RegisteredUser user, String role) {
        leftPanel.removeAll();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.setBackground(Color.WHITE);

        String[] columns = {"Name", "Location", "Time and Date", "Points Needed", "Points Earned"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);
        JTable table = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(table);

        PointsSystem.loadEventsTable(model, conn);

        JButton backButton = new JButton("Back");
        JButton refreshButton = new JButton("Refresh");
        JButton addButton = new JButton("Add Event");
        JButton editButton = new JButton("Edit Event");

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(backButton);
        topPanel.add(refreshButton);
        topPanel.add(addButton);
        topPanel.add(editButton);

        JButton deleteButton = null;
        if (role.equals("chairman")) {
            deleteButton = new JButton("Delete Event");
            topPanel.add(deleteButton);
        }

        backButton.addActionListener(e -> {
            if (role.equals("chairman")) {
                showChairmanDashboard(leftPanel, frame, conn, user);
            } else {
                showDirectorDashboard(leftPanel, frame, conn, user);
            }
        });

        refreshButton.addActionListener(e -> PointsSystem.loadEventsTable(model, conn));

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
                    boolean success = PointsSystem.addEvent(
                            conn,
                            nameField.getText().trim(),
                            locationField.getText().trim(),
                            timeField.getText().trim(),
                            Integer.parseInt(pointsNeededField.getText().trim()),
                            Integer.parseInt(pointsEarnedField.getText().trim())
                    );

                    if (success) {
                        JOptionPane.showMessageDialog(frame, "Event added successfully.");
                        PointsSystem.loadEventsTable(model, conn);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Error adding event.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input.");
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
                    boolean success = PointsSystem.editEvent(
                            conn,
                            eventName.trim(),
                            locationField.getText().trim(),
                            timeField.getText().trim(),
                            Integer.parseInt(pointsNeededField.getText().trim()),
                            Integer.parseInt(pointsEarnedField.getText().trim())
                    );

                    if (success) {
                        JOptionPane.showMessageDialog(frame, "Event updated successfully.");
                        PointsSystem.loadEventsTable(model, conn);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Event not found.");
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Invalid input.");
                }
            }
        });

        if (deleteButton != null) {
            deleteButton.addActionListener(e -> {
                String eventName = JOptionPane.showInputDialog(frame, "Enter event name to delete:");
                if (eventName == null || eventName.trim().isEmpty()) return;

                int confirm = JOptionPane.showConfirmDialog(
                        frame,
                        "Delete event " + eventName + "?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION
                );

                if (confirm == JOptionPane.YES_OPTION) {
                    boolean success = PointsSystem.deleteEvent(conn, eventName.trim());

                    if (success) {
                        JOptionPane.showMessageDialog(frame, "Event deleted.");
                        PointsSystem.loadEventsTable(model, conn);
                    } else {
                        JOptionPane.showMessageDialog(frame, "Event not found.");
                    }
                }
            });
        }

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
        PointsSystem.loadPointsTable(model, conn);

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

    public static void showModifyPointsPanel(JPanel leftPanel, JFrame frame, Connection conn, RegisteredUser user) {
        String input = JOptionPane.showInputDialog(frame, "Enter Student ID:");
        if (input == null || input.trim().isEmpty()) return;

        try {
            int studentID = Integer.parseInt(input.trim());

            String pointsInput = JOptionPane.showInputDialog(frame, "Enter points to ADD or REMOVE (use negative number to subtract):");
            if (pointsInput == null || pointsInput.trim().isEmpty()) return;

            int change = Integer.parseInt(pointsInput.trim());

            boolean success = PointsSystem.modifyMemberPoints(conn, studentID, change);

            if (success) {
                JOptionPane.showMessageDialog(frame, "Points updated.");
            } else {
                JOptionPane.showMessageDialog(frame, "Member not found.");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(frame, "Invalid input.");
        }
    }

    public static void main(String[] args) {
        defaultGUI();
    }
}