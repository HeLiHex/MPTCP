package org.example.network.interfaces;



public interface Endpoint extends NetworkNode {
    /**
     * A method that checks if the outputBuffer has any Packets
     *
     * @return True if the outputBuffer is empty
     */
    boolean outputBufferIsEmpty();


    boolean isConnected();


}
