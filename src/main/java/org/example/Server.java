package org.example;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {


    public static void main(String[] args) {
        String host = "localhost";
        int port = 6666;

        ServerSocket serverSocket = createServerSocket();
        bindServerSocket(serverSocket, host, port);

        while (true) {
            try {
                Socket firstClient = serverSocket.accept();//establishes connection
                System.out.println("connection established with " + firstClient.getInetAddress());

            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }


    public static ServerSocket createServerSocket() {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket();
        } catch (IOException e) {
            System.err.println("Error occurred trying to create a ServerSocket");
            e.printStackTrace();
        }
        return serverSocket;
    }

    public static void bindServerSocket(ServerSocket serverSocket, String hostToBeBound, int portToBeBound) {
        try {
            serverSocket.bind(new InetSocketAddress(hostToBeBound, portToBeBound));
            System.out.println("Server running on: " + serverSocket.getLocalSocketAddress());
        } catch (IOException e) {
            System.err.printf("Could not bind server to address: %s and port %d", hostToBeBound, portToBeBound);
            e.printStackTrace();
        }
    }
}

