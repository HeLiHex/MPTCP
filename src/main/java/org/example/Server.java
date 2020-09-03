package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends Thread {

    private static final String HOST_ADDRESS = "localhost";
    private static final int PORT = 6666;
    private ServerSocket serverSocket;
    private Logger logger = Logger.getLogger(Server.class.getName());

    public Server() {
        this.serverSocket = createServerSocket();
        bindServerSocket(this.serverSocket, HOST_ADDRESS, PORT);
    }

    public String getHostAddress() {
        return HOST_ADDRESS;
    }

    public int getPort() {
        return PORT;
    }

    private ServerSocket createServerSocket() {
        ServerSocket newServerSocket = null;
        try {
            newServerSocket = new ServerSocket();
        } catch (IOException e) {
            this.logger.log(Level.SEVERE, "Error occurred trying to create a ServerSocket");
            e.printStackTrace();
        }
        return newServerSocket;
    }

    private void bindServerSocket(ServerSocket serverSocket, String hostToBeBound, int portToBeBound) {
        try {
            serverSocket.bind(new InetSocketAddress(hostToBeBound, portToBeBound));
            String msg = "Server running on: " + serverSocket.getLocalSocketAddress();
            this.logger.log(Level.INFO, msg);
        } catch (IOException e) {
            String msg = "Could not bind server to address: " + hostToBeBound + " and port " + portToBeBound;
            this.logger.log(Level.SEVERE, msg);
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket firstClient = serverSocket.accept();//establishes connection
                String msg = "connection established with " + firstClient.getInetAddress();
                this.logger.log(Level.INFO, msg);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

