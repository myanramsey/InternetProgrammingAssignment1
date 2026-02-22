import java.io.*;
import java.net.*;

/**
 * Server - Listens for client connections, validates alphabetic input,and returns capitalized strings.
 *
 * Startup: java server [port_number]
 */
public class server {

    public static void main(String[] args) {
        // Validate command-line arguments
        if (args.length != 1) {
            System.out.println("Usage: java server [port_number]");
            System.exit(1);
        }

        int port = 0;
        try {
            port = Integer.parseInt(args[0]);
            if (port < 1 || port > 65535) {
                System.out.println("Error: Port number must be between 1 and 65535.");
                System.exit(1);
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid port number.");
            System.exit(1);
        }

        ServerSocket serverSocket = null;
        Socket clientSocket = null;

        try {
            // Create server socket and bind to the specified port
            serverSocket = new ServerSocket(port);
            System.out.println("Capital Converter Server started on port " + port);
            System.out.println("Waiting for client connection...");

            // Accept a client connection (one client at a time)
            clientSocket = serverSocket.accept();
            System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());

            // Set up input/output streams
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);

            // Send greeting to client
            out.println("Hello!");
            System.out.println("Sent: Hello!");

            // Main communication loop
            String receivedMessage;
            while ((receivedMessage = in.readLine()) != null) {
                System.out.println("Received from client: " + receivedMessage);

                // Check for termination signal
                if (receivedMessage.equalsIgnoreCase("bye")) {
                    out.println("disconnected");
                    System.out.println("Client sent 'bye'. Sending 'disconnected' and closing.");
                    break;
                }

                // Validate that the string contains only alphabetic characters (a-z, A-Z)
                if (isAllAlphabets(receivedMessage)) {
                    // Convert to uppercase and send back
                    String capitalized = receivedMessage.toUpperCase();
                    out.println(capitalized);
                    System.out.println("Sent capitalized: " + capitalized);
                } else {
                    // Send error message requesting retransmission
                    out.println("ERROR: Invalid input. Please send alphabets only (a-z). Try again.");
                    System.out.println("Invalid input detected. Requested retransmission.");
                }
            }

        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } finally {
            // Graceful shutdown
            try {
                if (clientSocket != null && !clientSocket.isClosed()) {
                    clientSocket.close();
                    System.out.println("Client socket closed.");
                }
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                    System.out.println("Server socket closed. Server exiting.");
                }
            } catch (IOException e) {
                System.out.println("Error closing sockets: " + e.getMessage());
            }
        }
    }

    /**
     * Checks whether all characters in the string are alphabetic (a-z or A-Z).
     *
     * @param str the input string to validate
     * @return true if all characters are alphabets, false otherwise
     */
    private static boolean isAllAlphabets(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        for (char c : str.toCharArray()) {
            if (!Character.isLetter(c)) {
                return false;
            }
        }
        return true;
    }
}
