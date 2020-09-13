package org.example.util;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkUtil {

    private static final Logger logger = Logger.getLogger(NetworkUtil.class.getName());


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

}
