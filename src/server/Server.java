package server;

import java.io.*;
import java.net.*;
import java.util.*;

public class Server {
    // Flag to control server state
    private boolean isRunning = true;
    //  Instantiate list for active client threads
    private Vector<MultiThread> clientConnections;
    //  Socket waits for client connections
    public static ServerSocket serversocket;
    // Int For Next Connection ID
    private int nextId = 0;
    // Port Server Will Be Listening To
    public static final int PORT = 8000;
    //public SendEmail email = new SendEmail();
    DBMS userDB;
    // -- Server Constructor  -- //
    public Server() {
        //  Construct the list of active client threads
        clientConnections = new Vector<MultiThread>();
        this.userDB = new DBMS();
        userDB.syncUserList();
        userDB.logoutAll(userDB);
        userDB.disconnectAll(userDB);
        //userDB.printUserList();   //  Unit Test
    }   //  --  End Server Constructor  -- //

    //  --  Start Server From GUI Method    -- //
    public static void startServer(){
        new Server();
    }   //  --  End Start Server Method -- //

    //  --  Stop Server Method  -- //
    public void stop() {
        isRunning = false;  // Set flag to stop the loop
        try {
            if (serversocket != null && !serversocket.isClosed()) {
                serversocket.close();  // Close the ServerSocket
            }

            // Interrupt all client connections
            for (MultiThread connection : clientConnections) {
                connection.interrupt();  // Assuming MultiThread extends Thread
            }
            clientConnections.clear();  // Clear the list of client connections
            userDB.logoutAll(userDB);
            userDB.disconnectAll(userDB);

            System.out.println("Server Stopped");
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    //  --  Get Port Method -- //
    public static int getPort() {
        return PORT;
    }   //  --  End Get Port Method -- //

    //  --  Peer Connection Creation Method --
    private void peerConnection(Socket socket) {
        //  Create a thread communication when client arrives
        Network networkConnection = new Network(nextId, socket);
        MultiThread connection = new MultiThread(networkConnection, socket, this);
        //System.out.println(networkConnection.getId());    //  Unit Test
        //System.out.println(connection.getId());   //  Unit Test

        //  Add the new thread to the active client threads list
        clientConnections.add(connection);
        //  Start the thread
        connection.start();
        //  Place some text in the area to let the server operator know what is going on
        System.out.println("SERVER: New Client Connection Received -\nNew Thread ID - \"" + nextId + "\"\n");
        ++nextId;
    }   //  --  End Peer Connection Method  -- //

    //  --  Remove ID of individual Client Object Method    --
    public void removeID(int ID) {  // Called by a ServerThread after a client is terminated
        //  Find the object belonging to the client thread being terminated
        for (int i = 0; i < clientConnections.size(); ++i) {
            MultiThread cc = clientConnections.get(i);
            long targetID = cc.getId();
            if (targetID == ID) {   //  If matching ID is found
                // Remove ID from the clientConnections list and the connection thread will terminate itself
                clientConnections.remove(i);
                //  Place some text in the area to let the server operator know what is going on
                System.out.println("SERVER: Client Connection Closed\nNew Thread ID - \"" + ID + "\"\n");
                break;
            }   //  End If
        }   //  End For
    }   //  --  End Remove ID Method    -- //

    //  --  Server Listens For New Connections Method  -- //
    public void listen() {
        try {
            serversocket = new ServerSocket(PORT);
            System.out.println("New Server Object Started: Port - " + PORT + "\nReady For Client Connections\n");
            while (isRunning) { //  Control infinite loop
                try {
                    Socket socket = serversocket.accept(); // Accept client connections
                    if (!isRunning) break; // Double-check in case the socket was closed
                    peerConnection(socket);
                } catch (IOException e) {
                    if (!isRunning) {
                        System.out.println("Server Socket Closed");
                        break; // Exit the loop if the server is stopping
                    } else {
                        System.out.println(e);
                    }   //  End Else
                }   //  End Catch
            }   //  End While
        } catch (IOException e) {
            System.out.println(e);
        } finally {
            try {
                if (serversocket != null && !serversocket.isClosed()) {
                    serversocket.close();
                }   //  End If
            } catch (IOException e) {
                System.out.println(e);
            }   //  End Catch
            //System.out.println("Server stopped listening.");  //  Unit Test
        }   //  End Finally
    }   //  --  End Listen Method   -- //

    //  -- CLIENT OPERATIONS -- //

    //  -- Log in to Server Method (1) -- //
    public String login(String username, String pass) {
        //  Login variables
        String response = "";
        int index = indexOfUser(username);
        if(index == -1) response = "1"; //  No username exists
        else {
            User account = User.userList.get(index);
            if (account.isLocked()) { // checks if acc is locked out
                response = "2"; // account is locked
            } else if (account.isLogged()) { // checks if account is already signed in
                response = "3"; // account is already signed in
            } else if (!account.getPassword().equals(pass)) {
                account.addStrike(userDB);
                response = "4" + account.getStrikes(); //  Incorrect password plus one strike
            } else {    //  Successful login
                account.setConnected(true, userDB);
                account.setLogged(true, userDB);
                account.resetStrikes(userDB);
                response = "0";
            }   //  End Else
        }   //  End Else
        return response;
    }   //  --  End Login Method    -- //

    //  --  Register New User Method (2)    -- //
    public String register(String username, String password, String email) {
        String response = "";
        int index = indexOfUser(username);
        if (index != -1) {
            response = "1"; //  Username exists
        } else {
            User account = new User(username, password, email); //  Create new user
            userDB.registerUser(account);   //  Add new user to database
            userDB.syncUserList();  //  Sync database to array list
            //System.out.println(User.userList.size()); //  Unit Test
            response = "0"; // successful registration
        }   //  End Else
        return response;
    }   //  --  End Register Method -- //

    //  --  Password Recovery Method (3)   -- //
    public String passwordRecovery(String username) {
        SendEmail email = new SendEmail();
        String response = "";
        int index = indexOfUser(username);
        if(index == -1) response = "1"; //  Username does not exists
        else {
            User account = User.userList.get(index);
            String address = account.getEmail(); // Getting user email
            //System.out.println(address);    //  Unit Test
            String newPassword = email.generateEmail(address); // sending email
            //System.out.println(newPassword);    //  Unit Test
            userDB.updateUserPassword(account, newPassword, userDB);    //  Setting accounts password to temp password
            response = "0"; //  Sending back to parseInput
        }   //  End Else
        return response;
    }   //  --  End Password Recovery Method    -- //

    //  --  Logout Method (4)   -- //
    public String logout(String username){
        String response = "";
        int index = indexOfUser(username);
        if (index == -1) response = "1"; //  Username does not exists
        else {
            User account = User.userList.get(index);
            account.setConnected(false,userDB);
            account.setLogged(false,userDB);
            response = "0";
        }   //  End Else
        return response;
    }   //  --  End Logout Method   -- //

    // -- Update Password Method -- //
    public String updatePassword(String username, String newPass){
        String response = "";
        int index = indexOfUser(username);
        if (index == -1){
            response = "1"; // Username does not exist
        } else {
            User account = User.userList.get(index);
            account.setPassword(newPass, userDB);
            response = "0"; // Successfully update password
        }
        return response;
    } // -- End New Password Method -- //

    // -- Shutdown Method -- //
    public String shutdown(String username){
        String response = "";
        int index = indexOfUser(username);
        if (index == -1) response = "1"; //  Username does not exists
        else {
            String logoutSuccess = logout(username);
            if(!logoutSuccess.equals("0")){
                response = "1Logout Error";
            } else {
                response = "5";
            }   //  End Else
        }   //  End Else
        return response;
    }   //  --  End Shutdown Method -- //

    //  --  Index Of User Method (-1 for no matches)    -- //
    public int indexOfUser(String username){
        for(int i = 0; i < User.userList.size(); ++i){
            User testMatch = User.userList.get(i);
            if(username.equals(testMatch.getUsername())){
                return i;
            }   //  End If
        }   //  End For
        return -1;
    }   //  --  End Index Of User Method    -- //
    //  --  Parse Input from Network Class Method   -- //
    // Using 0 and 1 for True and False responses in places applicable, extending beyond 0 and 1 when needed
    public String parseInput(String data){
        //System.out.println("Received data: " + data); //  Unit Test
        char operation;
        String result = "";
        String response = "";
        if(data != null) {
            operation = data.charAt(0); // grabbing operation from string
            System.out.println("Parsing Client Input - Operation received: " + operation);
            if(!data.isEmpty()) {
                result = data.substring(1);
                String[] info = result.split(":");
                String username;
                String password;
                //System.out.println("Entering if loop.");  //  Unit Test
                //System.out.println("Number of elements received: " + info.length);    //  Unit Test
                //System.out.println("Remaining info: " + result);  //  Unit Test
                switch (operation) {    //  Logic to decide server response
                    // 0
                    case '0':
                        System.out.println("Client Used \"Connect\"\nClient Input - \"" + result + "\""); //  Display Logic
                        response = "0"; // Connection Successful (or it would have been failed before here)
                        break;
                    case '1':
                        System.out.println("Client Used \"Login\" \nClient Input - \"" + result + "\"");   //  Display Logic
                        //  Gathering user information from the substring
                        username = info[0];
                        password = info[1];
                        System.out.println("User Info - \nUsername - \"" + username + "\"\nPassword - \"" + password + "\"");
                        //  Calling login function so the response can go back to Network
                        response = login(username, password);
                        //System.out.println(response); //  Unit Test
                        break;
                    case '2':
                        System.out.println("Client Used \"Register\"\nClient Input - \"" + result + "\"");    //  Display Logic
                        // Gathering registration information from the substring
                        String newUser = info[0]; // Assume info[0] contains the username
                        String newPass = info[1]; // Assume info[1] contains the password
                        String newEmail = info[2];  //  Assume info[2] contains email
                        System.out.println("New User Info - \nUsername - \"" + newUser + "\"\nPassword - \"" + newPass + "\"\nEmail - \"" + newEmail + "\"");
                        // Calling register function and storing the response
                        response = register(newUser, newPass, newEmail);
                        // Optionally print or log the response for debugging purposes
                        //System.out.println("Register response: " + response); //  Unit Test
                        break;
                    case '3':
                        System.out.println("Client Used \"Recover Password\"\nClient Input - \"" + result + "\"");    //  Display Logic
                        username = info[0];
                        response = passwordRecovery(username);
                        break;
                    case '4':
                        System.out.println("Client Used \"Logout\"\nClient Input - \"" + result + "\"");    //  Display Logic
                        username = info[0];
                        response = logout(username);
                        break;
                    case '5':
                        System.out.println("Client Used \"Disconnect\"\nClient Input - \"" + result + "\"");    //  Display Logic
                        response = "5";
                        break;
                    case '6':
                        System.out.println("Client Used \"Shutdown\"\nClient Input - \"" + result + "\"");    //  Display Logic
                        username = info[0];
                        response = shutdown(username);
                        break;
                    case '7':
                        System.out.println("Client Used \"Update Password\"\nClient Input - \"" + result + "\"");    //  Display Logic
                        username = info[0];
                        password = info[1];
                        response = updatePassword(username, password);
                        break;
                    case '8':
                        System.out.println("Client Used \"Server Application\"\nClient Input - \"" + result + "\"");    //  Display Logic
                        response = serverApplication();
                        break;
                    default : // In case the operation is not recognized for some reason
                        System.out.println("Client Used Unknown Operation \"" + operation + "\"\nClient Input - \"" + result + "\"");    //  Display Logic
                        response = (-1 + "ERROR parseInput: Unknown/Unrecognized Operation Requested");
                }   //  End Switch (operation)
            }   //  End If (data length > 1)
        }   //  End If (Data is not null)
        System.out.println("SERVER: Server.parseInput() - sending: " + response);
        return response;
    }
    public synchronized List<String> getLoggedInUsers() {
        List<String> loggedInUsers = new ArrayList<>();
        for (User account : User.userList) {
            if (account.isLogged()) {
                loggedInUsers.add(account.getUsername());
            }
        }
        return loggedInUsers;
    }
    public int getNumberOfLoggedInUsers(){
        return getLoggedInUsers().size();
    }
    public synchronized List<String> getLockedOutUsers() {
        List<String> LockedOutUsers = new ArrayList<>();
        for (User account : User.userList) {
            if (account.isLocked()) {
                LockedOutUsers.add(account.getUsername());
            }   //  End If
        }   //  End For
        return LockedOutUsers;
    }
    public int getNumberOfConnections(){
        return clientConnections.size();
    }
    public int getNumberOfLockedOutUsers(){
        return getLockedOutUsers().size();
    }
    public synchronized int getNumberOfRegisteredUsers() {
        return User.userList.size();
    }
    public String serverApplication(){
        return "0Good Job!";
    }
}
