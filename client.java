/**
 * to run: javac server.java client.java
 * (machine 2) java client <server-IP-or-hostname> [port_number]
 */

import java.net.*;
import java.io.*;
import java.util.Scanner;

public class client {

    static final int PORT = 7121; // port number (match server's)

    // socket and streams
    private Socket s = null;
    private BufferedReader in  = null;
    private PrintWriter out = null;
    private BufferedReader userInput = null;

    public client(String serverURL, int port) {
        try {
            // connect to the server
            System.out.println("Connecting to server: " + serverURL + " on port " + port + "...");
            s = new Socket(serverURL, port);
            System.out.println("Connected to server");

            // set up reader/writer on the socket's streams
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(), true); // auto flush
            userInput = new BufferedReader(new InputStreamReader(System.in));

            // receive and print the "Hello!" greeting from the server
            String serverMsg = in.readLine();
            System.out.println("Server: " + serverMsg);

            // statistics tracking (used for round-trip time measurements)
            int measurementCount = 0;
            double minRTT = Double.MAX_VALUE;
            double maxRTT = Double.MIN_VALUE;
            double sumRTT = 0.0;
            double sumSqRTT = 0.0;

            String userStr = "";

            // loop until user types "bye"
            while (true) {
                System.out.print("Enter a string (or 'bye' to quit): ");
                userStr = userInput.readLine();

                if (userStr == null) {
                    // handle EOF / keyboard disconnect
                    userStr = "bye";
                }

                if (userStr.equalsIgnoreCase("bye")) {
                    // send bye and wait for server's "disconnected" response
                    out.println("bye");
                    String response = in.readLine();
                    System.out.println("Server: " + response);
                    System.out.println("exit");
                    break;
                }

                // --- start timer ---
                long startTime = System.nanoTime();

                // send string to server
                out.println(userStr);

                // receive response from server
                String response = in.readLine();

                // --- stop timer ---
                long endTime = System.nanoTime();

                System.out.println("Server: " + response);

                // only record RTT if the server sent back a capitalized string
                if (!response.startsWith("There are characters")) {
                    double rttMs = (endTime - startTime) / 1_000_000.0; // convert ns -> ms
                    measurementCount++;
                    sumRTT   += rttMs;
                    sumSqRTT += rttMs * rttMs;
                    if (rttMs < minRTT) minRTT = rttMs;
                    if (rttMs > maxRTT) maxRTT = rttMs;

                    System.out.printf("Round-trip time: %.3f ms%n", rttMs);

                    // print statistics after every 5 successful measurements
                    if (measurementCount >= 5 && measurementCount % 5 == 0) {
                        printStatistics(measurementCount, minRTT, maxRTT, sumRTT, sumSqRTT);
                    }
                }
            }

            // print final statistics if we have at least one valid measurement
            if (measurementCount > 0) {
                System.out.println("\n--- Final RTT Statistics ---");
                printStatistics(measurementCount, minRTT, maxRTT, sumRTT, sumSqRTT);
            }

            // close all streams and socket
            s.close();
            in.close();
            out.close();
            userInput.close();

        } catch (IOException i) {
            System.out.println(i);
        }
    }

    /**
     * print min, mean, max, and standard deviation of round-trip times.
     *
     * @param count number of valid RTT measurements
     * @param min minimum RTT in ms
     * @param max maximum RTT in ms
     * @param sum sum of all RTTs in ms
     * @param sumSq sum of squares of all RTTs in ms^2
     */
    private void printStatistics(int count, double min, double max, double sum, double sumSq) {
        double mean   = sum / count;
        // population std dev: sqrt(E[x^2] - (E[x])^2)
        double stdDev = Math.sqrt((sumSq / count) - (mean * mean));

        System.out.println("\n--- RTT Statistics (" + count + " measurements) ---");
        System.out.printf("Min: %.3f ms%n", min);
        System.out.printf("Mean: %.3f ms%n", mean);
        System.out.printf("Max: %.3f ms%n", max);
        System.out.printf("StdDev: %.3f ms%n", stdDev);
        System.out.println("--------------------------------------------");
    }

    public static void main(String[] args) {
        // validate that both serverURL and port_number arguments are provided
        if (args.length != 2) {
            System.out.println("Usage: java client [serverURL] [port_number]");
            return;
        }

        String serverURL = args[0];
        int port;

        try {
            port = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Invalid port number: " + args[1]);
            return;
        }

        if (port != PORT) {
            System.out.println("Warning: expected port " + PORT + ", got " + port);
        }

        new client(serverURL, port);
    }
}