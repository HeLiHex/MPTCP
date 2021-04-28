package org.example.simulator.statistics;

public interface TCPStats {

    void packetSend();

    void packetRetransmit();

    void packetFastRetransmit();

    void ackReceived();

    int getNumberOfPacketsSent();

    int getNumberOfPacketsRetransmitted();

    int getNumberOfPacketsFastRetransmitted();

    int getNumberOfAcksReceived();

    double getGoodput();

    void setRtt(long rtt);

    void trackCwnd(int cwnd);

    void createCWNDChart();

}
