package org.example.protocol.window.receiving;

import org.example.data.Packet;
import org.example.network.interfaces.Endpoint;
import org.example.protocol.Connection;
import org.example.protocol.window.IWindow;
import org.example.protocol.window.sending.SendingWindow;
import org.example.util.BoundedQueue;

public interface ReceivingWindow extends IWindow, BoundedQueue<Packet> {

    /**
     * A method that receives incoming packets and returns true
     * if a packet was added to the received packets queue
     *
     * @param sendingWindow
     * @return true if a non ACK packet was received
     */
    boolean receive(SendingWindow sendingWindow);

    /**
     * A method that returns the packet to ACK
     *
     * @param endpointToReceiveAck the endpoint to receive the ACK
     * @return the packet to ACK
     */
    Packet ackThis(Endpoint endpointToReceiveAck);

    /**
     * A method that checks if a ACK should be sent or not
     * @return
     */
    boolean shouldAck();

    /**
     * A method that calculates the receiving window packet index
     * of the given packet
     *
     * @param packet to calculate packet index too
     * @param connection to use in the calculation
     * @return the packet index of the given packet
     */
    int receivingPacketIndex(Packet packet, Connection connection);

    /**
     * A method that checks if a packet is inside the receiving window
     *
     * @param packet packet to check
     * @param connection to use in calculation
     * @return true if the packet is inside the receiving window
     */
    boolean inReceivingWindow(Packet packet, Connection connection);

}
