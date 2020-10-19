package org.example.network;

import org.example.data.Packet;

import java.util.List;

public interface NetworkNode {


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
     * A method that returns a List of the NetworkNode's outgoing Channels
     *
     * @return List of outgoing Channels from this NetworkNode
     */
    List<Channel> getChannels();


    /**
     * A method that creates and adds a Channel to this and the given node.
     * Effectively creating two directed edges in each direction with their own individual attributes
     *
     * @param node to add Channel to
     */
    void addChannel(NetworkNode node);


    /**
     * A method that returns the unique Address associated with this NetworkNode
     *
     * @return unique Address
     */
    Address getAddress();


    /**
     * A method that returns and dequeues a Packet form the inputBuffer
     * @return the dequeued Packet
     */
    Packet dequeueInputBuffer();

    /**
     * A method that enqueues the given Packet to the inputBuffer
     * @param packet to be enqueued
     * @return True if successful
     */
    boolean enqueueInputBuffer(Packet packet);

    /**
     * A method that checks if the inputBuffer has any Packets
     * @return True if the inputBuffer is empty
     */
    boolean inputBufferIsEmpty();


}
