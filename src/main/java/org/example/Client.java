package org.example;


import java.io.IOException;
import java.net.*;
import java.util.*;

public class Client extends Thread{

    private ArrayList<Socket> sockets;
    private String host;
    private int port;

    public Client(){
        this.sockets = createSockets();
    }

    public Client(String host, int port){
        this.sockets = createSockets();
        setHostAndPort(host, port);
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setHostAndPort(String host, int port){
        this.setHost(host);
        this.setPort(port);
    }

    private boolean isValidPort(int port){
        return port > 0 && port < 65536;
    }

    public void connect(String host, int port) {
        if (host == null){
            System.out.println("No host was given");
            return;
        }
        if (!isValidPort(port)){
            System.out.println("Given port is not valid");
            return;
        }

        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
        for (Socket socket : this.sockets) {
            try {
                socket.connect(inetSocketAddress);
            }catch (IOException e){
                e.printStackTrace();
                System.err.println("Could not connect to host: " + inetSocketAddress.getHostName());
            }
        }
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


    public static List<Socket> createSockets(){
        List<Socket> sockets = new ArrayList<>();
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
            e.printStackTrace();
            System.err.println("Could not close socket: " + socket.getInetAddress().getHostName());
        }
    }

    public static void closeAllSockets(ArrayList<Socket> sockets) {
        for (Socket socket: sockets) {
            closeSocket(socket);
        }
    }

    @Override
    public void run() {
        this.connect(this.host, this.port);
    }

}
