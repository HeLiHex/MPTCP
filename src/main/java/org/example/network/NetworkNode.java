package org.example.network;

import org.example.protocol.util.Packet;

import java.util.List;

public interface NetworkNode {

    List<NetworkNode> getNeighbours();

    void addNeighbour(NetworkNode node);

    int getCost();

    NetworkNode getPath(NetworkNode destination);

    void updateRoutingTable();


    /**
     * This function should route the packet thorough the nodes until the packets destination is reached
     * @param packet
     */
    void route(Packet packet);

}
