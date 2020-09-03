package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends Thread {

    private final static String HOST_ADDRESS = "localhost";
    private final static int PORT = 6666;
    private ServerSocket serverSocket;
    private Logger logger;

    public Server() {
        this.serverSocket = createServerSocket();
        bindServerSocket(this.serverSocket, this.HOST_ADDRESS, this.PORT);
        this.logger = Logger.getLogger(Server.class.getName());
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
            this.logger.log(Level.INFO, "Server running on: " + serverSocket.getLocalSocketAddress());
        } catch (IOException e) {
            this.logger.log(Level.SEVERE,
                    "Could not bind server to address: " + hostToBeBound + " and port " + portToBeBound
            );
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket firstClient = serverSocket.accept();//establishes connection
                this.logger.log(Level.INFO,"connection established with " + firstClient.getInetAddress());
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

