package org.example.simulator.statistics;

public interface Stats {

    void packetSend();

    void packetRetransmit();

    void packetFastRetransmit();

    void packetLost();

    void packetDropped();

    void packetReceived();

    int getNumberOfPacketsSent();

    int getNumberOfPacketsRetransmitted();

    int getNumberOfPacketsLost();

    int getNumberOfPacketsDropped();

    int getNumberOfPacketsFastRetransmitted();

    int getNumberOfPacketsReceived();

}
