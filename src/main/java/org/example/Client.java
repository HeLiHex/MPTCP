package org.example;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class Client {

    public static void main(String[] args) throws IOException {
        String serverAddress = "localhost";
        int serverPort =  6666;

        //exploreNetworkInterfaces();


        ArrayList<Socket> sockets = createSockets();
        Socket socketOne = sockets.get(0);
        Socket socketTwo = sockets.get(1);

        socketOne.connect(new InetSocketAddress(serverAddress, serverPort));
        socketTwo.connect(new InetSocketAddress(serverAddress, serverPort));



        closeAllSockets(sockets);

    }

    public static void exploreNetworkInterfaces() throws IOException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface nif : Collections.list(nets)) {
            if (!nif.isUp()) {
                continue;
            }
            System.out.println(nif.getName());
            System.out.println("Hardware address: " + nif.getHardwareAddress());

            List<InterfaceAddress> iFaceList = nif.getInterfaceAddresses();
            for (InterfaceAddress iFace : iFaceList) {
                if(iFace.getAddress() instanceof Inet6Address) continue;
                System.out.println("Interface address: " + iFace);
                break;
            }

            Enumeration<InetAddress> addresses = nif.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet6Address){
                    continue;
                }
                System.out.println("IPv4: " + addr.getHostAddress());
            }

            System.out.println("MTU: " + nif.getMTU());
            System.out.println("Is virtual: " + nif.isVirtual());
            System.out.println("Is up: " + nif.isUp());
            System.out.println("Is loopback: " + nif.isLoopback());
            System.out.println("Is point to point: " + nif.isPointToPoint());
            System.out.println("Supports Multicast: " + nif.supportsMulticast());
            System.out.println();
        }

    }


    public static ArrayList<Socket> createSockets(){
        ArrayList<Socket> sockets = new ArrayList<>();
        Enumeration<NetworkInterface> nets = null;
        try {
            nets = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e) {
            //TODO error msg
            e.printStackTrace();
        }

        for (NetworkInterface nif: Collections.list(nets)) {
            Enumeration<InetAddress> nifAddresses = nif.getInetAddresses();
            InetSocketAddress inetSocketAddress;
            try{
                if(nifAddresses.nextElement() instanceof Inet4Address) continue;
                inetSocketAddress = new InetSocketAddress(nifAddresses.nextElement(), 0);
            }catch (NoSuchElementException e){
                //TODO explain
                continue;
            }

            Socket s = new Socket();
            try {
                s.bind(inetSocketAddress);
                System.out.println("Socket bound to: " + inetSocketAddress.getHostString() );
                sockets.add(s);
            } catch (IOException e) {
                //TODO error msg
                e.printStackTrace();
            }
        }
        return sockets;
    }

    public static void closeSocket(Socket socket){
        try{
            socket.close();
        }catch (IOException e){
            System.err.println("Could not close socket: " + socket.getInetAddress().getHostName());
        }
    }

    public static void closeAllSockets(ArrayList<Socket> sockets) {
        for (Socket socket: sockets) {
            closeSocket(socket);
        }
    }
}
