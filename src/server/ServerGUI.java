package server;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ServerGUI extends JFrame {
    private JButton startButton, stopButton, updateButton;
    private JTable usersTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel, registeredAccountsLabel, connectedAccountsLabel, loggedAccountsLabel;

    private Server serverInstance;
    private Thread serverThread;
    private boolean isServerRunning = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ServerGUI gui = new ServerGUI();
            gui.setVisible(true);
        });
    }
    public ServerGUI() {
        setTitle("Server Management Console");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        //  Start GUI in top right
        positionFrameInTopRightCorner();

        //  Listen for closing window
        addCloseListener(this);

        // Control panel
        JPanel controlPanel = new JPanel();
        startButton = new JButton("Start Server");
        stopButton = new JButton("Stop Server");
        updateButton = new JButton("Update");
        stopButton.setEnabled(false);

        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(updateButton);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));

        statusLabel = new JLabel("Server Status: Stopped");
        registeredAccountsLabel = new JLabel("Registered Accounts: 0");
        connectedAccountsLabel = new JLabel("Connected Accounts: 0");
        loggedAccountsLabel = new JLabel("Logged Accounts: 0");
        statusPanel.add(statusLabel);
        statusPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        statusPanel.add(registeredAccountsLabel);
        statusPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        statusPanel.add(connectedAccountsLabel);
        statusPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        statusPanel.add(loggedAccountsLabel);
        JPanel centeredStatusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        centeredStatusPanel.add(statusPanel);

        String[] columnNames = {"Logged-In Users", "Locked-Out Users"};
        tableModel = new DefaultTableModel(columnNames, 0);
        usersTable = new JTable(tableModel);
        JScrollPane tableScrollPane = new JScrollPane(usersTable);

        add(controlPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(centeredStatusPanel, BorderLayout.SOUTH);

        setupActionListeners();
    }
    private void addCloseListener(JFrame frame) {
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);  // Prevent automatic window closing
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                handleWindowClosing();
            }
        });
    }

    private void handleWindowClosing() {
        int confirm = JOptionPane.showConfirmDialog(null,
                "Are you sure you want to exit?",
                "Exit Confirmation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            stopServer(); // Notify the server about logout
            System.exit(0);  // Terminate the application
        }
    }
private void positionFrameInTopRightCorner() {
        // Get screen dimensions
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int screenWidth = screenSize.width;
        int screenHeight = screenSize.height;

        // Calculate x and y for top-right corner positioning
        int frameWidth = getWidth();
        int frameHeight = getHeight();
        int x = screenWidth - frameWidth;
        int y = 0;

        // Set the frame location
        setLocation(x, y);
    }


    private void setupActionListeners() {
        startButton.addActionListener(e -> startServer());
        stopButton.addActionListener(e -> stopServer());
        updateButton.addActionListener(e -> updateServerStatus());
    }
    private volatile boolean serverReady = false; // Flag to track server readiness
    private void startServer() {
        if (!isServerRunning) {
            serverThread = new Thread(() -> {
                try {
                    serverInstance = new Server();
                    serverInstance.listen();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(this,
                            "Error starting server: " + e.getMessage(),
                            "Server Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });
            serverThread.start();
            isServerRunning = true;
            statusLabel.setText("Server Status: Updating");

            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            // Start a thread for periodic updates
            startPeriodicUpdates();
        }
    }

    private void startPeriodicUpdates() {
        new Thread(() -> {
            while (isServerRunning) {
                try {
                    if (!serverReady) {
                        serverReady = true;
                        Thread.sleep(250);
                        statusLabel.setText("Server Status: Updating.");
                        Thread.sleep(250);
                        statusLabel.setText("Server Status: Updating..");
                        Thread.sleep(250);
                        statusLabel.setText("Server Status: Updating...");
                    } else {
                        statusLabel.setText("Server Status: Running");
                        SwingUtilities.invokeLater(this::updateServerStatus);
                    }
                    Thread.sleep(500); // Delay between updates
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }).start();
    }
    private void stopServer() {
        if (isServerRunning) {
            try {
                if (serverInstance != null) {
                    serverInstance.stop();
                }

                if (serverThread != null) {
                    serverThread.interrupt();
                }

                statusLabel.setText("Server Status: Stopped");

                startButton.setEnabled(true);
                stopButton.setEnabled(false);
                isServerRunning = false;
                serverReady = false; // Reset readiness flag
                clearServerStatus();
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error stopping server: " + e.getMessage(),
                        "Server Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateServerStatus() {
        if (isServerRunning && serverInstance != null) {
            try {
                // Fetch data from the server instance
                int registeredUsers = serverInstance.getNumberOfRegisteredUsers();
                int connectedUsers = serverInstance.getNumberOfConnections();
                int loggedUsers = serverInstance.getNumberOfLoggedInUsers();

                List<String> loggedInUsers = serverInstance.getLoggedInUsers();
                List<String> lockedOutUsers = serverInstance.getLockedOutUsers();

                registeredAccountsLabel.setText("Registered Accounts: " + registeredUsers);
                connectedAccountsLabel.setText("Connected Clients: " + connectedUsers);
                loggedAccountsLabel.setText("Logged Accounts: " + loggedUsers);

                tableModel.setRowCount(0);
                int maxRows = Math.max(loggedInUsers.size(), lockedOutUsers.size());
                for (int i = 0; i < maxRows; i++) {
                    String loggedInUser = i < loggedInUsers.size() ? loggedInUsers.get(i) : "";
                    String lockedOutUser = i < lockedOutUsers.size() ? lockedOutUsers.get(i) : "";
                    tableModel.addRow(new Object[]{loggedInUser, lockedOutUser});
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                        "Error updating server status: " + e.getMessage(),
                        "Update Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Server is not running. Please start the server first.",
                    "Update Error",
                    JOptionPane.WARNING_MESSAGE);
        }
    }
    private void clearServerStatus(){
        registeredAccountsLabel.setText("Registered Accounts: " + 0);
        connectedAccountsLabel.setText("Connected Clients: " + 0);
        loggedAccountsLabel.setText("Logged Accounts: " + 0);
        int rows = tableModel.getRowCount();
        int count = 0;
        while(count < rows){
            tableModel.removeRow(0);
            count++;
        }
    }
}