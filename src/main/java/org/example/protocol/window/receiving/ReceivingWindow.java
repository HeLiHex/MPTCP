package org.example.protocol.window.receiving;

import org.example.data.Packet;
import org.example.protocol.window.IWindow;
import org.example.protocol.window.sending.SendingWindow;
import org.example.util.BoundedQueue;

import java.util.Queue;

public interface ReceivingWindow extends IWindow, BoundedQueue<Packet> {

    boolean receive(SendingWindow sendingWindow);

    Packet ackThis();

    boolean shouldAck();

    Queue<Packet> getReceivedPackets();

}
