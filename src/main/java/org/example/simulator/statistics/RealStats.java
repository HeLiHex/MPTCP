package org.example.simulator.statistics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RealStats implements Stats{

    //private int numberOfPackets; //total number of packets to be served. not counting retransmissions
    private int numberOfPacketsSent; //total number of packets sent (both normal and retransmissions)
    private int numberOfPacketsRetransmitted; //total number of packets retransmitted
    private int numberOfPacketsLost; //total number of packets lost
    private int numberOfPacketsDropped; // total number of packets dropped
    private int numberOfPacketsFastRetransmitted; // total number of packets dropped
    private int numberOfPacketsReceived; //total number of packets received. Should be the same as numberOfPackets(!?)

    @Override
    public void packetSend() {
        this.numberOfPacketsSent++;
    }

    @Override
    public void packetRetransmit() {
        this.numberOfPacketsRetransmitted++;
    }

    @Override
    public void packetFastRetransmit() {
        this.numberOfPacketsFastRetransmitted++;
    }

    @Override
    public void packetLost() {
        this.numberOfPacketsLost++;
    }

    @Override
    public void packetDropped() {
        this.numberOfPacketsDropped++;
    }

    @Override
    public void packetReceived() {
        this.numberOfPacketsReceived++;
    }

    @Override
    public int getNumberOfPacketsSent() {
        return this.numberOfPacketsSent;
    }

    @Override
    public int getNumberOfPacketsRetransmitted() {
        return this.numberOfPacketsRetransmitted;
    }

    @Override
    public int getNumberOfPacketsLost() {
        return this.numberOfPacketsLost;
    }

    @Override
    public int getNumberOfPacketsDropped() {
        return this.numberOfPacketsDropped;
    }

    @Override
    public int getNumberOfPacketsFastRetransmitted() {
        return this.numberOfPacketsFastRetransmitted;
    }

    @Override
    public int getNumberOfPacketsReceived() {
        return this.numberOfPacketsReceived;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            String jsonString = mapper.writeValueAsString(this);
            return jsonString;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
