package org.example.network;

import org.example.protocol.util.Packet;

import java.util.List;

public interface NetworkNode extends Comparable<NetworkNode> {


    void updateRoutingTable();

    /**
     * This function should route the packet through the nodes until the packets destination is reached
     *
     * @param packet
     */
    void route(Packet packet);

    void deliverPackage(Packet packet);

    List<NetworkNode> getNeighbours();

    void addNeighbour(NetworkNode node);

    Address getAddress();

    int getCost();


}
