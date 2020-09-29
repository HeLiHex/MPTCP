package org.example.network;

import org.example.protocol.util.Packet;

import java.util.List;

public interface NetworkNode {

    String getAddress();

    List<NetworkNode> getNeighbours();

    void addNeighbour(NetworkNode node);

    int getCost();

    void updateRoutingTable();

    void deliverPackage(Packet packet);


    /**
     * This function should route the packet through the nodes until the packets destination is reached
     *
     * @param packet
     */
    void route(Packet packet);

}
