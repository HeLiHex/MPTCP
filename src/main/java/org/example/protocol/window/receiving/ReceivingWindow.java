package org.example.protocol.window.receiving;

import org.example.data.Packet;
import org.example.protocol.window.sending.SendingWindow;
import org.example.util.BoundedQueue;

import java.util.Queue;

public interface ReceivingWindow extends BoundedQueue<Packet> {

    boolean receive();

    Packet ackThis();

    Queue<Packet> getReceivedPackets();

}
