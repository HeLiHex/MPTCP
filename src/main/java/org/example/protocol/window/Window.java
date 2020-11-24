package org.example.protocol.window;

import org.example.data.Packet;

public interface Window {

    boolean isWaiting();

    void packetToAck(Packet packet);

    void receivedAck(Packet ack);

    int windowSize();



}
