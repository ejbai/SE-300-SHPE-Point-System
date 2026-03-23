import javax.swing.*;
import java.awt.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

// TODO:
//  make the login button functional and check against accounts table in database
//  make different views for each of the four users based on login, and buttons that do things in each of them such as show a list of things or ask for inputs (already have public view button but it doesn't do anything)
//  ...

public class GUI {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            JFrame frame = new JFrame("SHPE Point Tracker");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(1200, 600);

            JPanel mainPanel = new JPanel(new BorderLayout());

            //  LEFT PANEL
            JPanel leftPanel = new JPanel();
            leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
            leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 50, 0, 0));


            leftPanel.setOpaque(true);
            leftPanel.setBackground(Color.WHITE);

            JLabel label = new JLabel("View Upcoming Events!");
            JButton button = new JButton("View");

            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            button.setAlignmentX(Component.CENTER_ALIGNMENT);

            leftPanel.add(Box.createVerticalGlue());
            leftPanel.add(label);
            leftPanel.add(Box.createRigidArea(new Dimension(0, 10)));
            leftPanel.add(button);
            leftPanel.add(Box.createVerticalGlue());

            //  RIGHT PANEL (LOGIN)
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

            // login

            loginButton.addActionListener(e -> {

                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                String hashedPassword = hashPassword(password); // TODO: the hashing isn't working

                String rank = DatabaseConnection.checkLogin(username, hashedPassword);

                if(rank != null){

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

            //  SPLIT PANE (DIVIDER)
            JSplitPane splitPane = new JSplitPane(
                    JSplitPane.HORIZONTAL_SPLIT,
                    leftPanel,
                    rightPanel
            );

            splitPane.setDividerLocation(250);
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


    public static String hashPassword(String password) {

        try {

            MessageDigest md = MessageDigest.getInstance("SHA-256");

            byte[] hashBytes = md.digest(password.getBytes());

            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);

                if (hex.length() == 1) {
                    hexString.append('0');
                }

                hexString.append(hex);
            }

            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}