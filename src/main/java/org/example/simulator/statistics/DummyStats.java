package org.example.simulator.statistics;

public class DummyStats implements Stats{
    @Override
    public void packetSend() {

    }

    @Override
    public void packetRetransmit() {

    }

    @Override
    public void packetFastRetransmit() {

    }

    @Override
    public void packetLost() {

    }

    @Override
    public void packetDropped() {

    }

    @Override
    public void packetReceived() {

    }

    @Override
    public int getNumberOfPacketsSent() {
        return 0;
    }

    @Override
    public int getNumberOfPacketsRetransmitted() {
        return 0;
    }

    @Override
    public int getNumberOfPacketsLost() {
        return 0;
    }

    @Override
    public int getNumberOfPacketsDropped() {
        return 0;
    }

    @Override
    public int getNumberOfPacketsFastRetransmitted() {
        return 0;
    }

    @Override
    public int getNumberOfPacketsReceived() {
        return 0;
    }
}
