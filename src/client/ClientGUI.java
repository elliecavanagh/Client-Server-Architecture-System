package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ClientGUI {
    private Client client;
    private final Map<Integer, ImageIcon> backgroundCache = new HashMap<>();
    private final Random random = new Random();
    private JFrame connectFrame, loginFrame, dashboardFrame, forgottenPassFrame, registerFrame, updatePasswordFrame;
    boolean alreadyOn = false;
    // Coords for window locations
    int x, y;

    public static void main(String[] args) {
        new ClientGUI();
    }

    private void addCloseListener(JFrame frame) {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // Disable default close action
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleWindowClosing(e);
            }
        });
    }

    private void handleWindowClosing(java.awt.event.WindowEvent e) {
        int confirm = JOptionPane.showConfirmDialog(
                e.getWindow(), // Use the frame's window for the dialog
                "Are you sure you want to exit?",
                "Exit Confirmation",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            System.out.println("YES Option");
            client.logout();
            client.disconnect();
            System.out.println(e);
            System.exit(0); // Terminate the application
        } else {
            // Explicitly consume the event to prevent default behavior

        }
    }

    public ClientGUI() {
        client = new Client();
        preloadBackgrounds();
        showConnectWindow(500, 300);
    }   //  --  End Client GUI Constructor  --

    private void preloadBackgrounds() {
        try {
            backgroundCache.put(1, new ImageIcon(new URL("https://raw.githubusercontent.com/ayasmina/335-4/54b8694187f281efc4c680940b31dfad0a85ce7c/ConnectBackground1.gif")));
            backgroundCache.put(2, new ImageIcon(new URL("https://raw.githubusercontent.com/ayasmina/335-4/54b8694187f281efc4c680940b31dfad0a85ce7c/ConnectBackground2.gif")));
            backgroundCache.put(3, new ImageIcon(new URL("https://raw.githubusercontent.com/ayasmina/335-4/54b8694187f281efc4c680940b31dfad0a85ce7c/ConnectBackground3.gif")));
            backgroundCache.put(4, new ImageIcon(new URL("https://raw.githubusercontent.com/ayasmina/335-4/54b8694187f281efc4c680940b31dfad0a85ce7c/ForgotPasswordBackground.gif")));
            backgroundCache.put(5, new ImageIcon(new URL("https://raw.githubusercontent.com/ayasmina/335-4/47d97f9e32d1d5f4aff4a44f22801968f43d3782/LoginBackground.gif")));
            backgroundCache.put(6, new ImageIcon(new URL("https://raw.githubusercontent.com/ayasmina/335-4/54b8694187f281efc4c680940b31dfad0a85ce7c/NewPasswordBackground.gif")));
            backgroundCache.put(7, new ImageIcon(new URL("https://raw.githubusercontent.com/ayasmina/335-4/54b8694187f281efc4c680940b31dfad0a85ce7c/RegisterUserBackground.gif")));
            backgroundCache.put(8, new ImageIcon(new URL("https://raw.githubusercontent.com/ayasmina/335-4/47d97f9e32d1d5f4aff4a44f22801968f43d3782/ServerApp.gif")));
            backgroundCache.put(9, new ImageIcon(new URL("https://raw.githubusercontent.com/ayasmina/335-4/47d97f9e32d1d5f4aff4a44f22801968f43d3782/ServerDefaultBackground.gif")));


        } catch (Exception e) {
            System.err.println("Error preloading GIFs: " + e.getMessage());
        }
    }

    private void showConnectWindow(int newX, int newY) {
        // Select a random background from the cache
        int randomNumber = random.nextInt(1,3); // Randomly select 1, 2, or 3
        ImageIcon backgroundIcon = backgroundCache.get(randomNumber);

        connectFrame = new JFrame("Connect");
        addCloseListener(connectFrame); // Listen for closing window

        if (backgroundIcon != null) {
            JLabel backgroundLabel = new JLabel(backgroundIcon);
            backgroundLabel.setLayout(new GridBagLayout()); // Allow adding components over the image
            connectFrame.setContentPane(backgroundLabel);   // Set the JLabel as the content pane
        } else {
            System.err.println("No background available for: " + randomNumber);
        }

        connectFrame.setLocation(newX, newY);
        connectFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        connectFrame.setSize(400, 200);
        connectFrame.setLayout(new GridBagLayout());

        JLabel IPLabel = new JLabel("Server IP:");
        JTextField IPField = new JTextField(20);
        JButton connectButton = new JButton("Connect");

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        connectFrame.add(IPLabel, gbc);

        gbc.gridx = 1;
        connectFrame.add(IPField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        connectFrame.add(connectButton, gbc);

        connectButton.addActionListener(e -> {
            String serverIP = IPField.getText();
            if(serverIP.isEmpty()){
                JOptionPane.showMessageDialog(connectFrame, "Blank IP Field", "Connection Error", JOptionPane.ERROR_MESSAGE);
            } else {
                String result = client.connect(serverIP);
                char success = result.charAt(0);
                String output = result.substring(1);
                if (success == '0') {
                    JOptionPane.showMessageDialog(connectFrame, output);
                    connectFrame.dispose();
                    closingWindow(connectFrame);
                    showLoginWindow(this.x, this.y);
                } else {
                    JOptionPane.showMessageDialog(connectFrame, result, "Connection Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        connectFrame.setVisible(true);
    }

    private void showLoginWindow(int newX, int newY) {
        loginFrame = new JFrame("Login");
        addCloseListener(loginFrame);   //  Listen for closing window
        ImageIcon backgroundIcon = backgroundCache.get(5);
        JLabel backgroundLabel = new JLabel(backgroundIcon);
        backgroundLabel.setLayout(new GridBagLayout()); // Allow adding components over the image
        loginFrame.setContentPane(backgroundLabel);   // Set the JLabel as the content pane
        loginFrame.setLocation(newX, newY);
        loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        loginFrame.setSize(450, 400);
        loginFrame.setLayout(new GridBagLayout());

        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(20);
        JButton loginButton = new JButton("Login");
        JButton disconnectButton = new JButton("Disconnect");
        JLabel registerLabel = new JLabel("<html><a href='#'>Not registered? Register</a></html>");
        JLabel forgottenPassLabel = new JLabel("<html><a href='#'>Forgot Password?</a></html>");

        JCheckBox showPasswordCheckbox = new JCheckBox("Show Password");
        showPasswordCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showPasswordCheckbox.isSelected()) {
                    passwordField.setEchoChar((char) 0); // Show the password
                } else {
                    passwordField.setEchoChar('*'); // Hide the password
                }
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 1;
        loginFrame.add(usernameLabel, gbc);

        gbc.gridx = 1;
        loginFrame.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        loginFrame.add(passwordLabel, gbc);

        gbc.gridx = 1;
        loginFrame.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        loginFrame.add(showPasswordCheckbox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        loginFrame.add(loginButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        gbc.gridwidth = 2;
        loginFrame.add(disconnectButton, gbc);

        gbc.gridy = 6;
        loginFrame.add(registerLabel, gbc);

        gbc.gridy = 7;
        loginFrame.add(forgottenPassLabel, gbc);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String result = client.login(username, password);
            char success = result.charAt(0);
            String output = result.substring(1);
            if (success == '0') {
                JOptionPane.showMessageDialog(loginFrame, output);
                loginFrame.dispose();
                closingWindow(loginFrame);
                showDashboardWindow(x, y, 9);
            } else {
                JOptionPane.showMessageDialog(loginFrame, result, "Login Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        disconnectButton.addActionListener((e -> {
            String result = client.disconnect();
            JOptionPane.showMessageDialog(loginFrame,result);
            loginFrame.dispose();
            closingWindow(loginFrame);
            showConnectWindow(x, y);
        }));

        registerLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                openRegisterWindow(x, y);
            }
        });

        forgottenPassLabel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                openForgottenPassWindow(x, y);
            }
        });

        loginFrame.setVisible(true);
    }

    private void showDashboardWindow(int newX, int newY, int background) {
        dashboardFrame = new JFrame("Dashboard");
        addCloseListener(dashboardFrame); // Listen for closing window
        ImageIcon backgroundIcon = backgroundCache.get(background);
        JLabel backgroundLabel = new JLabel(backgroundIcon);
        backgroundLabel.setLayout(new GridBagLayout()); // Allow adding components over the image
        dashboardFrame.setContentPane(backgroundLabel); // Set the JLabel as the content pane
        dashboardFrame.setLocation(newX, newY);
        dashboardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        dashboardFrame.setSize(450, 400);

        // Semi-transparent container for header, buttons, and other components
        JPanel transparentBox = new JPanel(new GridBagLayout());
        transparentBox.setOpaque(true); // Make the background visible
        transparentBox.setBackground(new Color(255, 255, 255, 200)); // Semi-transparent white
        transparentBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        // Create the "Welcome" label with the specified font
        JLabel welcomeLabel = new JLabel("Welcome");
        welcomeLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));

        // GridBagConstraints for the components
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2; // Span across two columns if needed
        transparentBox.add(welcomeLabel, gbc); // Add welcome label to the transparent box

        // Buttons for various actions
        JButton shutdownButton = new JButton("Shutdown");
        JButton updatePasswordButton = new JButton("Update Password");
        JButton logoutButton = new JButton("Log Out");
        JButton serverAppButton = new JButton("Server Application");

        // Add buttons to transparentBox
        gbc.gridwidth = 1; // Reset gridwidth for buttons
        gbc.gridx = 0;
        gbc.gridy = 1;
        transparentBox.add(shutdownButton, gbc);

        gbc.gridy = 2;
        transparentBox.add(updatePasswordButton, gbc);

        gbc.gridy = 3;
        transparentBox.add(logoutButton, gbc);

        gbc.gridy = 4;
        transparentBox.add(serverAppButton, gbc);

        // Add transparentBox to backgroundLabel
        GridBagConstraints boxConstraints = new GridBagConstraints();
        boxConstraints.gridx = 0;
        boxConstraints.gridy = 1;
        boxConstraints.gridwidth = 2; // Allow box to span both columns
        backgroundLabel.add(transparentBox, boxConstraints);

        // Shutdown button functionality
        shutdownButton.addActionListener(e -> {
            String result = client.shutdown();
            char operation = result.charAt(0);
            result = result.substring(1);
            if (operation == '0') {
                JOptionPane.showMessageDialog(dashboardFrame, result);
                dashboardFrame.dispose();
                closingWindow(dashboardFrame);
                showConnectWindow(x, y);
            } else {
                JOptionPane.showMessageDialog(dashboardFrame, result);
            }
        });

        // Update Password button functionality
        updatePasswordButton.addActionListener(e -> showUpdatePasswordWindow(x, y));

        // Log out button functionality
        logoutButton.addActionListener(e -> {
            String result = client.logout();
            JOptionPane.showMessageDialog(dashboardFrame, result);
            dashboardFrame.dispose();
            showLoginWindow(x, y);
        });

        // Server Application button functionality
        serverAppButton.addActionListener(e -> {
            String result = client.serverApplication();
            char success = result.charAt(0);
            String output = result.substring(1);
            if (success == '0') {
                JOptionPane.showMessageDialog(dashboardFrame, output, "Server Application", JOptionPane.INFORMATION_MESSAGE);
                dashboardFrame.dispose();
                showDashboardWindow(x, y, 8);
            } else {
                JOptionPane.showMessageDialog(dashboardFrame, output, "Server Application Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Make the frame visible
        dashboardFrame.setVisible(true);
    }

    private void showUpdatePasswordWindow(int newX, int newY) {
        updatePasswordFrame = new JFrame("Update Password");
        ImageIcon backgroundIcon = backgroundCache.get(6); // Use a cached background
        JLabel backgroundLabel = new JLabel(backgroundIcon);
        backgroundLabel.setLayout(new GridBagLayout()); // Allow adding components over the image
        updatePasswordFrame.setContentPane(backgroundLabel); // Set the JLabel as the content pane
        updatePasswordFrame.setLocation(newX, newY);
        updatePasswordFrame.setSize(450, 400);

        // Semi-transparent container for input fields and buttons
        JPanel transparentBox = new JPanel(new GridBagLayout());
        transparentBox.setOpaque(true); // Make the background visible
        transparentBox.setBackground(new Color(255, 255, 255, 200)); // Semi-transparent white
        transparentBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        // Labels and input fields
        JLabel newPasswordLabel = new JLabel("New Password:");
        JPasswordField newPasswordField = new JPasswordField(20);

        JLabel verifyPasswordLabel = new JLabel("Verify Password:");
        JPasswordField verifyPasswordField = new JPasswordField(20);

        JButton updateButton = new JButton("Update");

        JCheckBox showPasswordCheckbox = new JCheckBox("Show Password");
        showPasswordCheckbox.addActionListener(e -> {
            if (showPasswordCheckbox.isSelected()) {
                newPasswordField.setEchoChar((char) 0); // Show the password
                verifyPasswordField.setEchoChar((char) 0);
            } else {
                newPasswordField.setEchoChar('*'); // Hide the password
                verifyPasswordField.setEchoChar('*');
            }
        });

        // Add components to the transparent box
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        gbc.gridx = 0;
        gbc.gridy = 0;
        transparentBox.add(newPasswordLabel, gbc);

        gbc.gridx = 1;
        transparentBox.add(newPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        transparentBox.add(verifyPasswordLabel, gbc);

        gbc.gridx = 1;
        transparentBox.add(verifyPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        transparentBox.add(showPasswordCheckbox, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        transparentBox.add(updateButton, gbc);

        // Add the transparent box to the background label
        backgroundLabel.add(transparentBox);

        // Update button functionality
        updateButton.addActionListener(e -> {
            String newPassword = new String(newPasswordField.getPassword());
            String verifyPassword = new String(verifyPasswordField.getPassword());

            if (newPassword.equals(verifyPassword)) {
                String result = client.updatePassword(newPassword);
                char operation = result.charAt(0);
                result = result.substring(1);
                if (operation == '0') {
                    JOptionPane.showMessageDialog(updatePasswordFrame, result);
                    updatePasswordFrame.dispose();
                } else {
                    JOptionPane.showMessageDialog(updatePasswordFrame, result);
                }
            } else {
                JOptionPane.showMessageDialog(updatePasswordFrame, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        updatePasswordFrame.setVisible(true);
    }

    private void openRegisterWindow(int newX, int newY) {
        // Set up the registration frame with background image
        registerFrame = new JFrame("Register");
        ImageIcon backgroundIcon = backgroundCache.get(7);
        JLabel backgroundLabel = new JLabel(backgroundIcon);
        backgroundLabel.setLayout(new GridBagLayout()); // Allow adding components over the image
        registerFrame.setContentPane(backgroundLabel);   // Set the JLabel as the content pane
        registerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        registerFrame.setSize(450, 400);
        registerFrame.setLayout(new GridBagLayout());
        registerFrame.setLocation(newX, newY);
        registerFrame.setVisible(true);

        JLabel headingLabel = new JLabel("Register New User");
        headingLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));
        headingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headingLabel.setForeground(Color.BLACK);

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setForeground(Color.BLACK);

        JPasswordField passwordField = new JPasswordField(20);
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.BLACK);
        JTextField usernameField = new JTextField(20);
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.BLACK);
        JTextField emailField = new JTextField(20);
        JButton registerButton = new JButton("Register");

        JCheckBox showPasswordCheckbox = new JCheckBox("Show Password");
        showPasswordCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (showPasswordCheckbox.isSelected()) {
                    passwordField.setEchoChar((char) 0); // Show the password
                } else {
                    passwordField.setEchoChar('*'); // Hide the password
                }
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(20, 20, 20, 20);

        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        registerFrame.add(headingLabel, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        registerFrame.add(emailLabel, gbc);

        // Add email field
        gbc.gridx = 0;
        gbc.gridy = 2;
        registerFrame.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        registerFrame.add(usernameLabel, gbc);

        // Add username field
        gbc.gridx = 0;
        gbc.gridy = 4;
        registerFrame.add(usernameField, gbc);

        // Add password label
        gbc.gridx = 0;
        gbc.gridy = 5;
        registerFrame.add(passwordLabel, gbc);

        // Add password field
        gbc.gridx = 0;
        gbc.gridy = 6;
        registerFrame.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        registerFrame.add(showPasswordCheckbox, gbc);

        // Add register button
        gbc.gridx = 0;
        gbc.gridy = 8;
        registerFrame.add(registerButton, gbc);

        registerButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                String email = emailField.getText();
                String successMsg = "User successfully registered.";

                String result = client.register(username, password, email);
                JOptionPane.showMessageDialog(registerFrame, result);

                // Close the registration frame
                if(result.equals(successMsg)) {
                    registerFrame.dispose();
                }
            }
        });

        registerFrame.setLocation(500, 300);
        registerFrame.setVisible(true);
    }

    private void openForgottenPassWindow(int newX, int newY) {
        forgottenPassFrame = new JFrame("Recovery");
        forgottenPassFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ImageIcon backgroundIcon = backgroundCache.get(4); // Use cached background
        JLabel backgroundLabel = new JLabel(backgroundIcon);
        backgroundLabel.setLayout(new GridBagLayout()); // Allow adding components over the image
        forgottenPassFrame.setContentPane(backgroundLabel); // Set the JLabel as the content pane

        forgottenPassFrame.setSize(450, 400);
        forgottenPassFrame.setLocation(newX, newY);

        // Semi-transparent container for input fields and buttons
        JPanel transparentBox = new JPanel(new GridBagLayout());
        transparentBox.setOpaque(true); // Ensure the background is visible
        transparentBox.setBackground(new Color(255, 255, 255, 200)); // Semi-transparent white
        transparentBox.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Add padding

        // Labels and input fields
        JLabel headingLabel = new JLabel("Recover Password");
        headingLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));
        headingLabel.setForeground(Color.BLACK);

        JLabel usernameLabel = new JLabel("Please Enter Username:");
        JTextField usernameField = new JTextField(20);
        JButton recoverButton = new JButton("Recover");

        // Set consistent styling for the button
        recoverButton.setFont(new Font("Arial", Font.PLAIN, 14)); // Font style and size
        recoverButton.setMargin(new Insets(10, 20, 10, 20)); // Padding around the text
        recoverButton.setPreferredSize(new Dimension(200, 40)); // Button size
        recoverButton.setBackground(Color.WHITE); // Set the background color to white
        recoverButton.setForeground(Color.BLACK); // Set the text color to black
        recoverButton.setFocusPainted(false); // Remove the focus border

        // Add components to the transparentBox
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        transparentBox.add(headingLabel, gbc);

        gbc.gridy++;
        transparentBox.add(usernameLabel, gbc);

        gbc.gridy++;
        transparentBox.add(usernameField, gbc);

        gbc.gridy++;
        transparentBox.add(recoverButton, gbc);

        // Add transparentBox to backgroundLabel
        GridBagConstraints boxConstraints = new GridBagConstraints();
        boxConstraints.gridx = 0;
        boxConstraints.gridy = 0;
        backgroundLabel.add(transparentBox, boxConstraints);

        // Recover button functionality
        recoverButton.addActionListener(e -> {
            String username = usernameField.getText();
            if (!username.isEmpty()) {
                String result = client.recoverPassword(username);
                char operation = result.charAt(0);
                result = result.substring(1);
                if (operation == '0') {
                    JOptionPane.showMessageDialog(forgottenPassFrame, result);
                    forgottenPassFrame.dispose();
                } else {
                    JOptionPane.showMessageDialog(forgottenPassFrame, result);
                }
            } else {
                JOptionPane.showMessageDialog(forgottenPassFrame, "Please enter a username!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Set the default button for the frame
        forgottenPassFrame.getRootPane().setDefaultButton(recoverButton);

        // Make the frame visible
        forgottenPassFrame.setVisible(true);
    }
    private void closingWindow(Window window){
        x = window.getLocation().x;
        y = window.getLocation().y;
    }
}

// first window that pops up should be "connect" i will ask for the IP and the connect button
// once connected a window pops up that asks to log in keeping the register link (that opens the register window if clicked) and the forgotten password ( that if clicked the recovery window pops open)
// if the user logs in then a window pops open called dashboard and it has a shutdown button that shuts down calls log out and disconnect and another button called update password that pops a window open asks to enter new password and another one to verify password entered  (compare the two fields) and a log out button
//then a big red button server application - calls new operation (server application) - returns a string (pops up new window w the string)