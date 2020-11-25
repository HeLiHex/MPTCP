package org.example.protocol.window;

import org.example.data.Packet;

public interface Window {

    boolean waitingForAck();

    void retransmit(Packet Packet);

    void add(Packet packet);

    void ackReceived(Packet ack);

    Packet getPacketToSend();

    void updateTimers();

    int windowSize();



}
