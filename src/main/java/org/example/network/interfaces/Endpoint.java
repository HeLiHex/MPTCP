package org.example.network.interfaces;

import org.example.data.Packet;

public interface Endpoint extends NetworkNode {

    /**
     * A method that returns and dequeues a Packet form the outputBuffer
     *
     * @return the dequeued Packet
     */
    Packet dequeueOutputBuffer();


    /**
     * A method that enqueues the given Packet to the outputBuffer
     *
     * @param packet to be enqueued
     * @return True if successful
     */
    boolean enqueueOutputBuffer(Packet packet);


    /**
     * A method that checks if the outputBuffer has any Packets
     *
     * @return True if the outputBuffer is empty
     */
    boolean outputBufferIsEmpty();


    /**
     * A method that returns the size of the outputBuffer
     *
     * @return size of the outputBuffer
     */
    int outputBufferSize();


    boolean isConnected();


}
