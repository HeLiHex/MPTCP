package org.example.protocol.window.sending;

import org.example.data.Packet;
import org.example.protocol.Connection;
import org.example.protocol.window.IWindow;
import org.example.util.BoundedQueue;

public interface SendingWindow extends IWindow, BoundedQueue<Packet> {

    boolean isWaitingForAck();

    void ackReceived(Packet ack);

    Packet send();

    boolean canRetransmit(Packet packet);

    Packet fastRetransmit();

    void increase();

    void decrease();

    boolean isSeriousLossDetected();

    int packetsInTransmission();

    int sendingPacketIndex(Packet packet);

    boolean inSendingWindow(Packet packet);

    int queueSize();

    Connection getConnection();

    boolean isQueueEmpty();

    @Override
    boolean offer(Packet packet);
}
