/**
 * to run: java server [port_number]
 */

import java.net.*;
import java.io.*;

public class server {

    static final int PORT = 7121; // port number we decided on (last 4 of UFID)

    // socket for the server and the connected client
    private ServerSocket ss = null;
    private Socket s        = null;

    // 2way streams: in for reading from client, out for writing back
    private BufferedReader in  = null;
    private PrintWriter    out = null;

    public server(int port) {  // this basically starts the server, accepts a client, and runs the loop
        try {
            ss = new ServerSocket(port);
            System.out.println("Server started");
            System.out.println("Waiting for a client ...");

            s = ss.accept();
            System.out.println("Client accepted");

            // set up reader/writer on the client socket's streams
            in  = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(), true); // if true, auto flush

            out.println("Hello!");    // assignment greeting for client
            String received = "";

            // keep loop reading from the client until they send "bye"
            while (!received.equalsIgnoreCase("bye")) {
                try {
                    received = in.readLine();
                    System.out.println("Client: " + received);

                    // extra check for ignore/keyboard disconnect
                    if (received.equalsIgnoreCase("bye")) {
                        out.println("disconnected");
                        break;
                    }

                    // check if all characters are alphabets
                    if (isAllAlphabets(received)) {
                        out.println(received.toUpperCase()); // capitalize and send back
                    } else {
                        // ask them to retype it since its not a-zA-Z
                        out.println("There are characters that are not alphabetical. Please retry.");
                    }

                } catch (IOException i) {
                    System.out.println(i);
                }
            }

            System.out.println("Closing connection");
            s.close();
            in.close();
            out.close();
            ss.close();

        } catch (IOException i) {
            System.out.println(i);
        }
    }

    private boolean isAllAlphabets(String str) { // only returns true if every char is a-z or A-Z
        if (str == null || str.isEmpty()) return false;
        for (char c : str.toCharArray()) {
            if (!Character.isLetter(c)) return false;
        }
        return true;
    }

    public static void main(String[] args) {
        // validate the correct port was passed in
        if (args.length != 1 || Integer.parseInt(args[0]) != PORT) {
            System.out.println("Usage: java server " + PORT);
            return;
        }
        new server(PORT);
    }
}