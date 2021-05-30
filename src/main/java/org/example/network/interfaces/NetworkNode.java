package org.example.network.interfaces;

import org.example.data.Packet;
import org.example.network.address.Address;
import org.example.network.Channel;

import java.util.List;

public interface NetworkNode {


    /**
     * A method that updates the routing table according to the cost
     */
    void updateRoutingTable();

    /**
     * A method that routes the packet to the next router on the path to it's destination
     *
     * @param packet to route
     */
    void route(Packet packet);


    long delay();


    /**
     * A method that returns a List of the NetworkNode's outgoing Channels
     *
     * @return List of outgoing Channels from this NetworkNode
     */
    List<Channel> getChannels();


    /**
     * * A method that creates and adds a Channel to the list of channels.
     *
     * @param node NetworkNode to create channel to
     * @param noiseTolerance
     * @param cost the cost of the channel used in routing
     */
    void addChannel(NetworkNode node, double noiseTolerance, int cost);


    /**
     * A method that returns the unique Address associated with this NetworkNode
     *
     * @return unique Address
     */
    Address getAddress();


    /**
     * A method that returns the first Packet form the inputBuffer without removing it from the buffer
     *
     * @return the dequeued Packet
     */
    Packet peekInputBuffer();


    /**
     * A method that returns and dequeues a Packet form the inputBuffer
     *
     * @return the dequeued Packet
     */
    Packet dequeueInputBuffer();

    /**
     * A method that enqueues the given Packet to the inputBuffer
     *
     * @param packet to be enqueued
     * @return True if successful
     */
    boolean enqueueInputBuffer(Packet packet);

    /**
     * A method that checks if the inputBuffer has any Packets
     *
     * @return True if the inputBuffer is empty
     */
    boolean inputBufferIsEmpty();

    /**
     * A method that returns the size of the inputBuffer
     *
     * @return size of the inputBuffer
     */
    int inputBufferSize();

    List<Channel> getChannelsUsed();

    void run();


}
