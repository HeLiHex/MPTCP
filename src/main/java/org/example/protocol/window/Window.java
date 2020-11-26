package org.example.protocol.window;

import org.example.data.Packet;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public interface Window extends BlockingQueue<WindowEntry> {

    boolean waitingForAck();

    void retransmit(Packet Packet);

    void add(Packet packet);

    void ackReceived(Packet ack);

    Packet getPacketToSend();

    void updateTimers();

    int windowSize();



}
