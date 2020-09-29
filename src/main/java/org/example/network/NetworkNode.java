package org.example.network;

import org.example.protocol.util.Packet;

import java.util.List;
import java.util.UUID;

public interface NetworkNode extends Comparable<NetworkNode> {

    UUID getAddress();

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
