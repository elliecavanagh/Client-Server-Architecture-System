package server;

import java.util.ArrayList;

public class User {
    private String username;
    private String password;
    private String email;
    private boolean connected;
    private boolean logged;
    private boolean locked;
    private int strikes;

    public static ArrayList<User> userList = new ArrayList<>();

    public User(String user, String pass, String add) {
        this.username = user;
        this.password = pass;
        this.email = add;
        this.connected = false;
        this.logged = false;
        this.strikes = 0;
        this.locked = false;
    }
    public User(String username, String password, String email, int connected, int loggedIn, int strikes, int lockedOut) {
        this.username = username;
        this.password = password;
        this.email = email;
        if(connected == 0){
            this.connected = false;
        } else {
            this.connected = true;
        }
        if(loggedIn == 0){
            this.logged = false;
        } else {
            this.logged = true;
        }
        this.strikes = strikes;
        if(lockedOut == 0){
            this.locked = false;
        } else {
            this.locked = true;
        }
    }
    // Getters and setters
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username, DBMS dbms) {
        if (dbms.updateStringField("Username", username, this.username)) {
            this.username = username;
        }
    }


    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password, DBMS dbms) {
        if (dbms.updateStringField("Password", password, this.username)) {
            this.password = password;
        }
    }
    public String getEmail() {
        return this.email;
    }
    public void setEmail(String email, DBMS dbms) {
        if (dbms.updateStringField("Email", email, this.username)) {
            this.email = email;
        }
    }
    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected, DBMS dbms) {
        if (dbms.updateIntField("Connected", connected ? 1 : 0, this.username)) {
            this.connected = connected;
        }
    }
    public boolean isLogged() {
        return this.logged;
    }

    public void setLogged(boolean logged, DBMS dbms) {
        if (dbms.updateIntField("LoggedIn", logged ? 1 : 0, this.username)) {
            this.logged = logged;
        }
    }

    public boolean isLocked() {
        return this.locked;
    }

    public void setLocked(boolean locked, DBMS dbms) {
        if (dbms.updateIntField("LockedOut", locked ? 1 : 0, this.username)) {
            this.locked = locked;
        }
    }

    public int getStrikes() {
        return this.strikes;
    }
    public void addStrike(DBMS dbms) {
        if (dbms.updateIntField("Strikes", this.getStrikes() + 1, this.username)) {
            this.strikes++;
            if(strikes == 3){
                setLocked(true,dbms);
            }
        }
    }
    public void resetStrikes(DBMS dbms) {
        if (dbms.updateIntField("Strikes", 0, this.username)) {
            this.strikes = 0;
        }
    }

    // Method to format user details into a readable string for MySQL
    public String getUserDetails() {
        return "User Details: " +
                "Username = '" + username + '\'' +
                ", Password = '" + password + '\'' +
                ", Email = '" + email + '\'' +
                ", Connected = " + connected +
                ", Logged = " + logged +
                ", LockedOut = " + locked +
                ", Strikes = " + strikes;
    }

    // Print all users in the list
    public static void printUserList() {
        for (User user : userList) {
            System.out.println(user.getUserDetails());
        }
    }
}