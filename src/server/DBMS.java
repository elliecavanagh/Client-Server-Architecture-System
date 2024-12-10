package server;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.ResultSetMetaData;

//  !!!WARNING: MAKE SURE THE JDBC CONNECTOR JAR IS IN THE BUILD PATH!!!
public class DBMS {
    //  Username and password for MySQL workbench connection
    private static final String USERNAME = "CassCo";
    private static final String PASSWORD = "Password.";
    // Objects to be used for database access
    private Connection connection = null;
    private Statement statement = null;
    private ResultSet result = null;
    //  Connect to the "UserDatabase" database scheme,
    //  Default port is 3306 (jdbc:mysql://<ip address>:<port>/<SCHEMA>)
    private final String SCHEMA = "sys";
    private final String TABLE = "users";
    private final String URL = "jdbc:mysql://127.0.0.1:3306/" + SCHEMA;
    //  Constructor for DBMS
    public DBMS () {
        try {
            // Make connection to the database
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            // These will Send queries to the database
            statement = connection.createStatement();
            result = statement.executeQuery("SELECT VERSION()");
            if (result.next()) {
                //System.out.println("MySQL version: " + result.getString(1) + "\n=====================\n");    //  Unit Test
            }   //  End If
        }   //  End Try
        catch (SQLException ex) {
            //  Handle any errors
            handleSQLException(ex);
        }   //  End Catch
    }   //  End DBMS Constructor
    // Centralized synchronization method
    public void syncUserList() {
        String query = "SELECT * FROM " + TABLE;
        try (PreparedStatement ps = connection.prepareStatement(query);
             ResultSet resultSet = ps.executeQuery()) {
            User.userList.clear();
            while (resultSet.next()) {
                String username = resultSet.getString("Username");
                String password = resultSet.getString("Password");
                String email = resultSet.getString("Email");
                int connected = resultSet.getInt("Connected");
                int loggedIn = resultSet.getInt("LoggedIn");
                int strikes = resultSet.getInt("Strikes");
                int lockedOut = resultSet.getInt("LockedOut");
                User.userList.add(new User(username, password, email, connected, loggedIn, strikes, lockedOut));
            }
            //System.out.println("User list synchronized successfully.");   //  Unit Test
        } catch (SQLException e) {
            handleSQLException(e);
        }
    }
    public boolean updateStringField(String fieldName, String newValue, String username) {
        String query = "UPDATE " + TABLE + " SET " + fieldName + " = ? WHERE Username = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, newValue);
            ps.setString(2, username);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                syncUserList();
                return true;
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }
    public boolean updateIntField(String fieldName, int newValue, String username) {
        String query = "UPDATE " + TABLE + " SET " + fieldName + " = ? WHERE Username = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setInt(1, newValue);
            ps.setString(2, username);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                syncUserList();
                return true;
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }
    public User selectUser(String username) {
        String query = "SELECT * FROM " + TABLE + " WHERE Username = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getString("Username"), rs.getString("Password"), rs.getString("Email"));
                }
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return null;
    }
    public boolean deleteUser(String username) {
        String query = "DELETE FROM " + TABLE + " WHERE Username = ?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, username);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                syncUserList();
                return true;
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }
    public boolean registerUser(User user) {
        if (selectUser(user.getUsername()) != null) {
            System.out.println("Username already exists!");
            return false;
        }
        String query = "INSERT INTO " + TABLE + " (Username, Password, Email, Connected, LoggedIn, Strikes, LockedOut) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getEmail());
            ps.setInt(4,0);
            ps.setInt(5,0);
            ps.setInt(6,0);
            ps.setInt(7,0);
            ps.executeUpdate();
            syncUserList();
            return true;
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }
    public boolean updateUserPassword(User user, String newPassword, DBMS userDB) {
        String queryUpdate = "UPDATE " + TABLE + " SET Password = ? WHERE Username = ?";
        try (PreparedStatement ps = connection.prepareStatement(queryUpdate)) {
            ps.setString(1, newPassword);
            ps.setString(2, user.getUsername());
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
                user.resetStrikes(userDB);
                user.setLocked(false, userDB);
                syncUserList(); // Synchronize after update
                return true;
            }
        } catch (SQLException e) {
            handleSQLException(e);
        }
        return false;
    }   //  --  End Update User Password Method --
    //  --  Disconnect All Connected Users Method   --
    public void disconnectAll(DBMS userDB){
        for(int i = 0; i < User.userList.size(); ++i){
            User.userList.get(i).setConnected(false, userDB);
        }   //  End For
    }       //  --  End Disconnect All Connected Users Method   --
    //  --  Logout All Logged Users Method   --
    public void logoutAll(DBMS userDB){
        for(int i = 0; i < User.userList.size(); ++i){
            User.userList.get(i).setLogged(false, userDB);
        }   //  End For
    }   //  --  End Logout All Users Method --
    //  --  Close Connection Method --
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }   //  End If
        }   //  End Try
        catch (SQLException e) {
            System.out.println(e);
        }   //  End Catch
    }   //  --  End Close Method    --
    //  --  Handle SQL Exceptions Method    --
    private void handleSQLException(SQLException e) {
        System.err.println("SQLException: " + e.getMessage());
        System.err.println("SQLState: " + e.getSQLState());
        System.err.println("VendorError: " + e.getErrorCode());
    }   //  --  End Handle SQL Exception Method --
    public int getConnectedUsers(){
        int total = 0;
        for(int i = 0; i < User.userList.size(); ++i){
            User checkUser = selectUser(User.userList.get(i).getUsername());
            if(checkUser.isConnected()){
                total++;
            }
        }
        return total;
    }
    /* Unit Testing
    public void printResultSet(ResultSet resultSet) {
        try {
            // Metadata contains how many columns in the data
            ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
            //  Number of fields in table
            int numberOfColumns = resultSetMetaData.getColumnCount();
            System.out.println("columns: " + numberOfColumns);  //  Display Logic
            //  Loop through the ResultSet one row at a time
            //  !!NOTE: The ResultSet starts at index 1!!
            while (resultSet.next()) {
                ArrayList<String> userData = new ArrayList<>();
                // Loop through the columns of the ResultSet (Starting at 1)
                for (int i = 1; i < numberOfColumns; ++i) {
                    String columnData = resultSet.getString(i);
                    userData.add(columnData);
                    System.out.print(columnData + "\t");  // Optional display logic
                }   //  End For
                System.out.println(); // End of row
                logUser(userData);
            }   // End While
        }   //  End Try
        catch (SQLException ex) {
            //  Handle any errors
            handleSQLException(ex);
        }   //  End Catch
    }   //  End Print Result
    public void logUser(ArrayList<String> userData){
        try {
            if (userData.size() >= 3) {  // Ensure enough fields are available
                User newUser = new User(userData.get(0), userData.get(1), userData.get(2));
                User.userList.add(newUser);  // Add the user to the static list
            } else {
                System.out.println("Insufficient user data to log.");
            }
        } catch (Exception e) {
            System.out.println("Failed to create or log user: " + e.getMessage());
        }
    }
*/
    /* Unit Test
    public void printUserList() {
        if (User.userList.isEmpty()) {
            System.out.println("No users available.");
        } else {
            System.out.println("Current Users:");
            for (User user : User.userList) {
                System.out.println(user.getUserDetails());
            }
        }
    }
*/
}   //  END DBMS CLASS