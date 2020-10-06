package org.example.network;

import org.example.data.Packet;

import java.util.List;

public interface NetworkNode extends Comparable<NetworkNode> {


    /**
     * A method that updates the routing table according to the cost
     */
    void updateRoutingTable();

    /**
     * A method that routes the packet to the next router on the path to it's destination
     *
     * @param packet
     */
    void route(Packet packet);


    /**
     * A method that delivers the packet to the next NetworkNode on the path to it's destination
     *
     * @param packet
     */
    void channelPackage(Packet packet);

    /**
     * A method that returns a list og the neighbouring NetworkNodes
     *
     * @return a list of neighbouring NetworkNodes
     */
    List<NetworkNode> getNeighbours();

    /**
     * A mathod that adds a NetworkNode as neighbour to this NetworkNode
     *
     * @param node to be added as neighbour
     */
    void addNeighbour(NetworkNode node);

    /**
     * A method that returns the unique Address associated with this NetworkNode
     *
     * @return unique Address
     */
    Address getAddress();

    /**
     * A method that returns the cost of visiting this node
     *
     * @return the cost of traversing to this node
     */
    int getCost();


    Packet dequeueOutputBuffer();

    boolean enqueueOutputBuffer(Packet packet);

    boolean outputBufferIsEmpty();

    Packet dequeueInputBuffer();

    boolean enqueueInputBuffer(Packet packet);

    boolean inputBufferIsEmpty();


}
