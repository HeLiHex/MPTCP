package org.example.protocol.window.sending;

import org.example.data.Packet;
import org.example.protocol.window.IWindow;
import org.example.util.BoundedQueue;

public interface SendingWindow extends IWindow, BoundedQueue<Packet> {

    boolean isWaitingForAck();

    void ackReceived(Packet ack);

    Packet send();

    boolean canRetransmit(Packet packet);

}
