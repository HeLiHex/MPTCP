package org.example.protocol.window.receiving;

import org.example.data.Packet;
import org.example.protocol.window.IWindow;
import org.example.protocol.window.sending.SendingWindow;
import org.example.util.BoundedQueue;

import java.util.Queue;

public interface ReceivingWindow extends IWindow, BoundedQueue<Packet> {

    /**
     * A method that receives incoming packets and returns true if a packet was added to the received packets queue
     *
     * @param sendingWindow
     * @return true if a non ACK packet was received
     */
    boolean receive(SendingWindow sendingWindow);

    Packet ackThis();

    boolean shouldAck();

    Queue<Packet> getReceivedPackets();

    int receivingPacketIndex(Packet packet);

    boolean inReceivingWindow(Packet packet);

}
