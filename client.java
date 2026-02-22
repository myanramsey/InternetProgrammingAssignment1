import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Client - Connects to the server, sends alphabetic strings,receives capitalized responses, and measures round-trip time.
 *
 * Startup: java client [serverURL] [port_number]
 */
public class client {


    public static void main(String[] args) {
        // Validate command-line arguments
        if (args.length != 2) {
            System.out.println("Usage: java client [serverURL] [port_number]");
            System.exit(1);
        }

        String serverURL = args[0];
        int port = 0;
        try {
            port = Integer.parseInt(args[1]);
            if (port < 1 || port > 65535) {
                System.out.println("Error: Port number must be between 1 and 65535.");
                System.exit(1);
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid port number.");
            System.exit(1);
        }

        Socket socket = null;
        Scanner scanner = new Scanner(System.in);
        // List to store round-trip times for successful (valid alphabet) transmissions
        ArrayList<Double> rttList = new ArrayList<>();

        try {
            // Connect to the server
            System.out.println("Connecting to server at " + serverURL + ":" + port + "...");
            socket = new Socket(serverURL, port);
            System.out.println("Connected to server.");

            // Set up input/output streams
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

            // Receive and print the greeting from the server
            String serverGreeting = in.readLine();
            System.out.println("Server says: " + serverGreeting);

            // Main communication loop
            while (true) {
                System.out.print("\nEnter a string (or 'bye' to quit): ");
                String userInput = scanner.nextLine();

                // Check for termination input
                if (userInput.equalsIgnoreCase("bye")) {
                    // Start timer, send "bye", wait for "disconnected"
                    out.println("bye");
                    String response = in.readLine();
                    if (response != null && response.equals("disconnected")) {
                        System.out.println("exit");
                    }
                    break;
                }

                // Start round-trip timer
                long startTime = System.nanoTime();

                // Send the string to the server
                out.println(userInput);

                // Receive server response
                String response = in.readLine();

                // Stop timer
                long endTime = System.nanoTime();

                if (response == null) {
                    System.out.println("Connection lost. Server closed unexpectedly.");
                    break;
                }

                System.out.println("Server response: " + response);

                // Only record RTT for valid (non-error) responses
                if (!response.startsWith("ERROR")) {
                    double rttMs = (endTime - startTime) / 1_000_000.0; 
                    rttList.add(rttMs);
                    System.out.printf("Round-trip time: %.3f ms%n", rttMs);
                    System.out.printf("Successful measurements so far: %d%n", rttList.size());
                }
            }

        } catch (UnknownHostException e) {
            System.out.println("Error: Unknown host - " + e.getMessage());
        } catch (ConnectException e) {
            System.out.println("Error: Could not connect to server - " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        } finally {
            // Print round-trip time statistics if we have measurements
            if (rttList.size() >= 5) {
                printStatistics(rttList);
            } else {
                System.out.println("Not enough valid measurements to display statistics " +
                        "(need at least 5, got " + rttList.size() + ").");
            }

            // Graceful shutdown
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                    System.out.println("Socket closed. Client exiting.");
                }
            } catch (IOException e) {
                System.out.println("Error closing socket: " + e.getMessage());
            }

            scanner.close();
        }
    }

    
    private static void printStatistics(ArrayList<Double> rttList) {
        int n = rttList.size();
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double sum = 0.0;

        // Compute min, max, and sum
        for (double rtt : rttList) {
            if (rtt < min) min = rtt;
            if (rtt > max) max = rtt;
            sum += rtt;
        }

        double mean = sum / n;

        // Compute standard deviation
        double varianceSum = 0.0;
        for (double rtt : rttList) {
            varianceSum += Math.pow(rtt - mean, 2);
        }
        double stdDev = Math.sqrt(varianceSum / n);

        System.out.println("\n========== Round-Trip Time Statistics ==========");
        System.out.printf("Number of measurements : %d%n", n);
        System.out.printf("Minimum RTT            : %.3f ms%n", min);
        System.out.printf("Maximum RTT            : %.3f ms%n", max);
        System.out.printf("Mean RTT               : %.3f ms%n", mean);
        System.out.printf("Standard Deviation     : %.3f ms%n", stdDev);

        System.out.println("================================================");
    }
}
