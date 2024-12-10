package client;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

//  Core Program Logic
//  Client Class creates a connection and allows the user to perform operations
public class Client {
    //  Client variables
    private static String HOST;
    private boolean clientIsConnected = false;
    private boolean clientIsLoggedIn = false;
    private boolean passwordIsValid = false;
    private boolean emailIsValid = false;

    public String username;

    public Network clientConnection;
    // --   Client Constructor for GUI to build Client Object   --
    public Client() {
        HOST = "";
    }   //  --  End Client Constructor  --

    //  --  CLIENT OPERATIONS   --

    //  !!NOTE: Operations are identified via a character key!!
    //  Operations Key: 0=Connect, 1=Login, 2=Register User, 3=Password Recovery,
    //                  4=Logout, 5=Disconnect, 6=Shutdown, 7=Update Password, 8=Server Application

    // --   Connect Clients to Server Method (0)    --
    public String connect(String host) {
        //  Connection variables
        String outputGUI = "";
        this.HOST = host;
        String request = "0" + HOST;   //  Append operation character key
        String response = "";
        if (clientIsConnected) {  //  Prevent connecting if connection already exists
            outputGUI = "Already connected to " + HOST;
            System.out.println(outputGUI);    //  Display Logic
        } else {    //  Attempt connection
            try {
                clientConnection = new Network(HOST); //  Client creates connection request
                response = clientConnection.send(request); //   Client sends connect request
                if (response == null) {  //  Failed connection
                    outputGUI = "Error connecting to " + HOST;
                    System.out.println(outputGUI);    //  Display Logic
                    clientIsConnected = false;
                } else {    //  Successful connection
                    outputGUI = "0Connection successful to " + HOST;
                    System.out.println(outputGUI);   //  Display Logic
                    clientIsConnected = true;
                }   //  End Else
            } catch (UnknownHostException e){
                outputGUI = "Host " + HOST + " is unavailable.";
            } catch (SocketTimeoutException e){
                outputGUI = "Connection to " + HOST + " has timed out.";
            } catch (IOException e) {
                outputGUI = "Server Not Available";
            }
        }   //  End Else
        return outputGUI;
    }   //  --  End Connect Method  --

    //  --  Login Request Method (1)    --
    public String login(String username, String password) {
        //  Login variables
        String outputGUI = "";
        String request = "1" + username + ":" + password + ":" + "Error";  //  Append operation character key
        String response = "";
        //  Check if Client is connected but not logged in
        if (clientIsConnected && !clientIsLoggedIn) {   //  If Client is connected but not logged in
            response = clientConnection.send(request);
            System.out.println("CLIENT receive: " + response);   //  Display Logic
            char readServerOperation = response.charAt(0);
            switch (readServerOperation){   //  Read Server response with first character key
                case '0':   //  '0'=Success
                    outputGUI = "0User successfully signed in.";
                    System.out.println(outputGUI);  //  Display Logic
                    clientIsLoggedIn = true;
                    this.username = username;
                    break;
                case '1':   //  '1'=Failed, no matching username
                    outputGUI = "No username matching our records.";
                    System.out.println(outputGUI);  //  Display Logic
                    break;
                case '2':   //  '2'=Failed, three strikes and user's account is locked
                    outputGUI = "Account is locked. Please go through password recovery.";
                    System.out.println(outputGUI);  //  Display Logic
                    break;
                case '3':   //  '3'=Failed due to Server issue
                    outputGUI = "Account is already logged in.";
                    System.out.println(outputGUI);  //  Display Logic
                    break;
                case '4':   //  '4'=Failed, wrong password and plus one strike
                    char strikesData = response.charAt(1);
                    int strikes = strikesData - '0';
                    outputGUI = "Password is incorrect. You now have " + strikes + " strike(s).\nYour account will lock at 3 strikes.\nYou have " + (3 - strikes) +  " attempts remaining." ;
                    System.out.println(outputGUI);  //  Display Logic
                    break;
                default:
                    outputGUI = "Error";
                    System.out.println(outputGUI);  //  Display Logic
                    break;
            }   //  End Switch
        } else if (!clientIsConnected) {    //  If Client is not connected but trying to log in
            outputGUI = "Please connect to the server first.";
            System.out.println(outputGUI);  //  Display Logic
        } else {    //  If login attempted when client is connected and already logged in
            outputGUI = "Already logged in.";
            System.out.println(outputGUI);  //  Display Logic
        }   //  End Else
        return outputGUI;
    }   //  --  End Login Method    --

    //  --  Register A New User Method (2)  --
    public String register(String newUsername, String userPassword, String userEmail) {
        //  Register variables
        String outputGUI = "";
        String request = "2" + newUsername + ":" + userPassword + ":" + userEmail;    //  Append operation character key
        if (clientIsConnected){ //  If Client is connected
            passwordIsValid = RegexEmail.validPassword(userPassword);   //  Check valid password
            emailIsValid = RegexEmail.validEmailAddress((userEmail));   //  Check valid email format
            if (passwordIsValid && emailIsValid) {  //  If user input had a valid password and email
                // Send the registration data to the server
                String response = clientConnection.send(request);
                System.out.println("CLIENT receive: " + response);  //  Display Logic
                //  Handle server response
                char readServerOperation = response.charAt(0);
                switch (readServerOperation) {  //  Read Server response with first character key
                    case '0':   //  '0'=Success
                        outputGUI = "User successfully registered.";
                        System.out.println("User successfully registered.");    //  Display Logic
                        break;
                    case '1':   //  '1'=Failed due to duplicate username
                        outputGUI = "Username already exists.";
                        System.out.println("Username already exists."); //  Display Logic
                        break;
                    default:    //  Failed for some other reason
                        outputGUI = "Registration failed. Please try again.";
                        System.out.println("Registration failed. Please try again.");   //  Display Logic
                        break;
                }   //  End Switch
            } else if (!passwordIsValid){   //  If password is invalid
                outputGUI = "Please enter a valid password.";
            } else if (!emailIsValid){  //  If email is invalid
                outputGUI = "Please enter a valid email.";
            } else {    //  If Client is not connected
                System.out.println("Please connect to the server first.");
            }   //  End Else

        }   //  End Else If
        return outputGUI;
    }   //  --  End Register    --

    //  --  Recover Password Method (3) --
    public String recoverPassword(String username) {
        //  Recover password variables
        String outputGUI = "";
        String request = "3" + username;    //  Append operation character key
        if (clientIsConnected) {    //  If Client is connected
            String response = clientConnection.send(request);
            //  Simulate sending a temporary password to the user's registered email
            System.out.println("CLIENT receive: " + response);  //  Display Logic
            //  Handle server response
            char readServerOperation = response.charAt(0);
            switch (readServerOperation) {  //  Read Server response with first character key
                case '0':   //  '0'=Success
                    outputGUI = "0Temporary password sent to the email associated with username:\n" + username;
                    System.out.println(outputGUI);  //  Display Logic
                    break;
                case '1':   //  '1'=Failed, no matching usernames in database
                    outputGUI = "1No account with that user exists.";
                    System.out.println(outputGUI);  //  Display Logic
                    break;
                default:    //  Failed for some other reason
                    outputGUI = -1 + "An error occurred.";
                    System.out.println(outputGUI);  //  Display Logic
            }   //  End Switch
        } else {    //  If Client is not connected
            System.out.println("Please connect to the server first.");
        }   //  End Else
        return outputGUI;
    }   //  --  End Recover Password Method --

    //  --  Logout (Without Disconnecting) Method (4)   --
    public String logout() {
        //  Logout variables
        String outputGUI = "";
        String request = "4" + username;   //  Append operation character key
        String response = "";
        if (clientIsLoggedIn) { //  If client is logged in
            response = clientConnection.send(request);  //  Send logout request
            System.out.println("CLIENT receive: " + response);  //  Display Logic
            //  Handle server response
            char readServerOperation = response.charAt(0);
            //  Read Server response with first character key
            if (readServerOperation == '0') {   //  '0'=Success
                outputGUI = "Client Logged Out Successfully";
                System.out.println(outputGUI);  //  Display Logic
                clientIsLoggedIn = false;
            } else {    //  If response is not a '0' then logout failed for some reason
                outputGUI = "An error occurred.";
                System.out.println(outputGUI);  //  Display Logic
            }   //  End Else
        } else {    //  If Client is not logged in
            System.out.println("Not logged in.");   //  Display Logic
        }   //  End Else
        return outputGUI;
    }   //  --  End Logout Method   --

    //  --  Disconnect Client from Server Method (5)    --
    public String disconnect() {
        //  Disconnect variables
        String outputGUI = "";
        String request = "5disconnect"; //  Append operation character key
        String response = "";
        if (clientIsConnected) {    //  If Client is connected
            response = clientConnection.send(request); // Send disconnect request
            System.out.println("CLIENT receive: " + response);
            if (response.charAt(0) == '0'){ //  If server disconnects
                outputGUI = "Disconnected from " + HOST;
                System.out.println(outputGUI);  //  Display Logic
                clientIsConnected = false;
                clientIsLoggedIn = false;   //  Automatically log out on disconnect
            } else {    //  If disconnect fails
                outputGUI = "Error disconnecting from " + HOST;
                System.out.println(outputGUI);  //  Display Logic
            }   //  End Else
        } else {    //  If Client is not connected
            System.out.println("Not connected.");
            outputGUI = "No connection found.";
        }   //  End Else
        return outputGUI;
    }   //  --  End Disconnect Method   --

    //  --  Shutdown Client Method (6) --
    public String shutdown(){
        String outputGUI = "";
        String request = "6" + username;
        String response = "";
        if(username != null){
            response = clientConnection.send(request);  // Send shutdown request
        } else {
            response = "3";
        }
        switch(response.charAt(0)){
            case '0':   //  Successful Shutdown
                outputGUI = "0Shutdown Successful";
                System.out.println(outputGUI);
                break;
            case '1':   //  Failed Logout
                outputGUI = "1Shutdown Failed due to Faulty Logout";
                System.out.println(outputGUI);
                break;
            case '2':   //  Failed Disconnect
                outputGUI = "2Shutdown Failed due to Faulty Disconnect";
                System.out.println(outputGUI);
                break;
            default:
                outputGUI = "3Unknown Error";
                System.out.println(outputGUI);
                break;
        }   //  End Switch
        clientIsLoggedIn = false;
        clientIsConnected = false;
        return outputGUI;
    }   //  --  End Shutdown Method --

    //  --  Update Password Method (7)  --
    public String updatePassword(String newPassword){
        String outputGUI = "";
        String response = "";
        passwordIsValid = RegexEmail.validPassword(newPassword);   //  Check valid password
        if (!passwordIsValid) {   //  If password is invalid
            outputGUI = "1Please enter a valid password.";
        } else {
            String request = "7" + username + ":" + newPassword;
            if (clientIsConnected && clientIsLoggedIn) {    //  If Client is connected
                response = clientConnection.send(request);  // Send disconnect request
                System.out.println("CLIENT receive: " + response);
                //  Handle server response
                char readServerOperation = response.charAt(0);
                //  Read Server response with first character key
                if (readServerOperation == '0') {   //  '0'=Success
                    outputGUI = "0Password successfully updated.";
                    System.out.println(outputGUI);  //  Display Logic
                } else {
                    outputGUI = "1Unsuccessful Update";
                }   //  End Else
            }   //  End If
        }   //  End Else
        return outputGUI;
    }   //  --  End  Update Password Method --

    //  --  Server Application Example Method (8)   --
    public String serverApplication(){
        String outputGUI = "";
        String response = "";
        String request = "8" + "example";
        if (clientIsLoggedIn) { //  If client is logged in
            response = clientConnection.send(request);  //  Send "Application" request
            System.out.println("CLIENT receive: " + response);  //  Display Logic
            //  Handle server response
            char readServerOperation = response.charAt(0);
            //  Read Server response with first character key
            if (readServerOperation == '0') {   //  '0'=Success
                outputGUI = "0Successful Application Usage";
                System.out.println(outputGUI);  //  Display Logic
            } else {    //  If response is not a '0' then operation failed for some reason
                outputGUI = "1An error occurred.";
                System.out.println(outputGUI);  //  Display Logic
            }   //  End Else
        } else {    //  If Client is not logged in
            System.out.println("2Not logged in.");   //  Display Logic
        }   //  End Else
        return outputGUI;
    }   //  --  End Server Application Method   --

}   //  END CLIENT CLASS