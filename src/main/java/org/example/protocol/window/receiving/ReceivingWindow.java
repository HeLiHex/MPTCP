package org.example.protocol.window.receiving;

import org.example.data.Packet;
import org.example.network.interfaces.Endpoint;
import org.example.protocol.Connection;
import org.example.protocol.window.IWindow;
import org.example.protocol.window.sending.SendingWindow;
import org.example.util.BoundedQueue;

public interface ReceivingWindow extends IWindow, BoundedQueue<Packet> {

    /**
     * A method that receives incoming packets and returns true if a packet was added to the received packets queue
     *
     * @param sendingWindow
     * @return true if a non ACK packet was received
     */
    boolean receive(SendingWindow sendingWindow);

    Packet ackThis(Endpoint endpointToReceiveAck);

    boolean shouldAck();

    int receivingPacketIndex(Packet packet, Connection connection);

    boolean inReceivingWindow(Packet packet, Connection connection);

}
