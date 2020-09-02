package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server extends Thread{

    private final static String HOST_ADDRESS = "localhost";
    private final static int PORT = 6666;
    private ServerSocket serverSocket;

    public Server(){
        this.serverSocket = createServerSocket();
        bindServerSocket(this.serverSocket, this.HOST_ADDRESS, this.PORT);
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
            System.err.println("Error occurred trying to create a ServerSocket");
            e.printStackTrace();
        }
        return newServerSocket;
    }

    private void bindServerSocket(ServerSocket serverSocket, String hostToBeBound, int portToBeBound) {
        try {
            serverSocket.bind(new InetSocketAddress(hostToBeBound, portToBeBound));
            System.out.println("Server running on: " + serverSocket.getLocalSocketAddress());
        } catch (IOException e) {
            System.err.printf("Could not bind server to address: %s and port %d", hostToBeBound, portToBeBound);
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Socket firstClient = serverSocket.accept();//establishes connection
                System.out.println("connection established with " + firstClient.getInetAddress());
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

