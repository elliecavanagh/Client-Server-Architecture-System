package server;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
public class MultiThread extends Thread {
    private boolean clientIsConnected;
    private int id;
    private BufferedReader datain;
    private DataOutputStream dataout;
    private Server server;
    private Socket socket;
    private Network network;

    public MultiThread(Network network, Socket socket, Server server) {
        this.network = network;
        this.id = network.getId();
        this.socket = socket;
        this.server = server;
        clientIsConnected = true;
        //  Create the stream I/O objects on top of the socket
        try {
            datain = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            dataout = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public long getId(){
        return this.id;
    }

    public void run () {
        // Server thread runs until the client terminates the connection
        while (clientIsConnected) {
            try {
                String txtOut = "";
                /*  always receives a String object with a newline (\n)
                    on the end due to how BufferedReader readLine() works.
                    The client adds it to the user's string but the BufferedReader
                    readLine() call strips it off   */
                // Using receive() instead of datain.readLine() cause...idk
                String txtIn = network.receive();
                if(txtIn != null) {
                    System.out.println("\nSERVER: Receive - \"" + txtIn + "\"");

                    // Sending txtIn to server instance of Server to parse the input and go through the operations
                    // txtOut is the response that parseInput returns after Server completes a process
                    txtOut = server.parseInput(txtIn);
                    //System.out.println("MT txtOut = " + txtOut); // -- debugging in case of response errors
                    if (txtOut == null || txtOut.isEmpty()) {
                        System.out.println("ERROR: Response is empty");
                        socket.setSoTimeout(5000); // prevents client from waiting for forever and ever and ever
                    } else {
                        if (txtOut.charAt(0) == '5') { // Checking for disconnect message before responding
                            // Writing Final Response
                            System.out.println("SERVER: Sending Disconnect Message - \"" + txtOut + "\"\n");
                            dataout.writeBytes("0Success");
                            dataout.flush();

                            //Closing Streams
                            clientIsConnected = false;
                            server.removeID(id);
                            datain.close();
                            System.out.println("SERVER: Disconnected Client\nMultiThread ID - " + getId()); //  Display Logic
                        } else {
                            System.out.println("SERVER: Sending - \"" + txtOut + "\"\n");
                            dataout.writeBytes(txtOut + "\n");
                            dataout.flush();
                        }
                    }
                } else{
                    clientIsConnected = false;
                }
            }   //  End Try
            catch(IOException e) {
                e.printStackTrace();
                clientIsConnected = false;
            }
        }
    }
}