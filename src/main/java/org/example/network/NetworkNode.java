package org.example.network;

import org.example.protocol.util.Packet;

import java.util.List;
import java.util.UUID;

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

    //todo change to address
    Address getAddress();

    int getCost();


}
