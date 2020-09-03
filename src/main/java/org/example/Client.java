package org.example;


import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Client extends Thread{

    private List<Socket> sockets;
    private String host;
    private int port;
    private Logger logger = Logger.getLogger(Client.class.getName());

    public Client(){
        this.sockets = createSockets();
    }

    public Client(String host, int port){
        this.sockets = createSockets();
        setHostAndPort(host, port);
    }

    protected void setHost(String host) {
        this.host = host;
    }

    protected void setPort(int port) {
        if(isValidPort(port)){
            this.port = port;
            return;
        }
        this.logger.log(Level.SEVERE, "Given port is invalid");
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public void setHostAndPort(String host, int port){
        this.setHost(host);
        this.setPort(port);
    }

    protected boolean isValidPort(int port){
        return port > 0 && port < 65536;
    }

    public void connect(){
        connect(this.host, this.port);
    }

    public void connect(String host, int port) {
        if (host == null){
            this.logger.log(Level.WARNING, "No host was given");
            return;
        }
        if (!isValidPort(port)){
            this.logger.log(Level.SEVERE, "Given port is not valid");
            return;
        }

        InetSocketAddress inetSocketAddress = new InetSocketAddress(host, port);
        for (Socket socket : this.sockets) {
            try {
                socket.connect(inetSocketAddress);
            }catch (IOException e){
                e.printStackTrace();
                String msg = "Could not connect to host: " + inetSocketAddress.getHostName();
                this.logger.log(Level.WARNING, msg);
            }
        }
    }


    public void exploreNetworkInterfaces() throws IOException {
        Enumeration<NetworkInterface> nets = NetworkInterface.getNetworkInterfaces();
        for (NetworkInterface nif : Collections.list(nets)) {
            if (!nif.isUp()) {
                continue;
            }
            this.logger.log(Level.INFO, nif.getName());
            String hardwareAddressMsg = "Hardware address: " + nif.getHardwareAddress();
            this.logger.log(Level.INFO, hardwareAddressMsg);

            List<InterfaceAddress> iFaceList = nif.getInterfaceAddresses();
            for (InterfaceAddress iFace : iFaceList) {
                if(iFace.getAddress() instanceof Inet6Address) continue;
                String iFaceAddressMsg = "Interface address: " + iFace;
                this.logger.log(Level.INFO, iFaceAddressMsg);
                break;
            }

            Enumeration<InetAddress> addresses = nif.getInetAddresses();
            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet6Address){
                    continue;
                }
                String ipMsg = "IPv4: " + addr.getHostAddress();
                this.logger.log(Level.INFO, ipMsg);
            }

            String mtuMsg = "MTU: " + nif.getMTU();
            this.logger.log(Level.INFO, mtuMsg);

            String isVirtualMsg = "Is virtual: " + nif.isVirtual();
            this.logger.log(Level.INFO, isVirtualMsg);

            String isUpMsg = "Is up: " + nif.isUp();
            this.logger.log(Level.INFO, isUpMsg);

            String isLoopbackMsg = "Is loopback: " + nif.isLoopback();
            this.logger.log(Level.INFO, isLoopbackMsg);

            String isPointToPointMsg = "Is point to point: " + nif.isPointToPoint();
            this.logger.log(Level.INFO, isPointToPointMsg);

            String supportsMulticatsMsg = "Supports Multicast: " + nif.supportsMulticast();
            this.logger.log(Level.INFO, supportsMulticatsMsg);
        }

    }


    public List<Socket> createSockets(){
        List<Socket> newSockets = new ArrayList<>();
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
                String msg = "Socket bound to: " + inetSocketAddress.getHostString();
                this.logger.log(Level.INFO, msg);
                newSockets.add(s);
            } catch (IOException e) {
                //TODO error msg
                e.printStackTrace();
            }
        }
        return newSockets;
    }

    public void closeSocket(Socket socket){
        try{
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
            String msg = "Could not close socket: " + socket.getInetAddress().getHostName();
            this.logger.log(Level.WARNING, msg);
        }
    }

    public void closeAllSockets(List<Socket> sockets) {
        for (Socket socket: sockets) {
            closeSocket(socket);
        }
    }

    @Override
    public void run() {
        this.connect(this.host, this.port);
    }

}
