package client;

import java.io.*;
import java.net.*;

// Client: Send, then Receive/listen
// Server: Receive/Listen then Send
// Alternating modes

public class Network {
    private static final int PORT = 8000;

    private String name;
    private int id;

    // handling peer to peer communication
    private BufferedReader datain;
    private DataOutputStream dataout;

    private Socket socket;

    // In given code: this was the public Client() method in Client.java
    // Client Network Object
    public Network(String host) throws UnknownHostException, SocketTimeoutException, IOException{
        try {
            // -- construct the peer to peer socket
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, PORT), 1000);
            // -- wrap the socket in stream I/O objects
            datain = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dataout = new DataOutputStream(socket.getOutputStream());
        } catch (UnknownHostException e) {
            System.out.println("Host " + host + " at port " + PORT + " is unavailable.");
            throw new UnknownHostException("Host " + host + " at port " + PORT + " is unavailable.");
            //System.exit(1);
        }  catch (SocketTimeoutException e ){
            System.out.println("Connect to " + host + " has timed out.");
            throw new SocketTimeoutException("Connect to " + host + " has timed out.");
            //System.exit(1);
        } catch (IOException e) {
            System.out.println("Unable to create I/O streams.");
            throw new IOException("Unable to create I/O streams.");
            //System.exit(1);
        }
    }

    //In given code: this was the ConnectionThread Constructor
    // -- creates I/O objects on top of the socket
    // Server Network Object
    public Network(int id, Socket socket) {
        this.id = id;
        this.name = Integer.toString(id);

        try {
            datain = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dataout = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    // ClientGUI -> Client -> Network -> Server -> Network -> Client -> ClientGUI
    // Client -> Network - clientConnection.send(String)
    // Network -> Server - server.parseInput(txtIn)
    // Server -> Network - the return message from parseInput()
    public String send(String msg) {
        String rtnmsg = "";

        try {
            // send String to Server
            dataout.writeBytes(msg + "\n"); // write string to bytes
            dataout.flush(); // send string to server


            rtnmsg = ""; // empty string for response
            do { // read for input while the response string is empty
                socket.setSoTimeout(5000); // Timeout of 5 seconds - makes it so the client wont wait forever
                // and ever if something is wrong
                rtnmsg = datain.readLine();
            } while (rtnmsg.equals(""));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return rtnmsg;
    }

    public int getId(){
        return this.id;
    }

    public String receive(){
        String res = "";
        try {
            res = datain.readLine();
        } catch (IOException e){
            e.printStackTrace();
            System.exit(1);
        }
        return res;
    }
}