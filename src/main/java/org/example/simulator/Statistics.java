package org.example.simulator;

import org.example.data.Packet;

public class Statistics {

    private int numberOfPackets; //total number of packets to be served. not counting retransmissions
    private int numberOfPacketsSent; //total number of packets sent (both normal and retransmissions)
    private int numberOfPacketsRetransmitted; //total number of packets retransmitted
    private int numberOfPacketsLost; //total number of packets lost
    private int numberOfPacketsReceived; //total number of packets received. Should be the same as numberOfPackets(!?)


    public Statistics() {
        this.numberOfPackets = 0;
        this.numberOfPacketsSent = 0;
        this.numberOfPacketsRetransmitted = 0;
        this.numberOfPacketsLost = 0;
        this.numberOfPacketsReceived = 0;
    }

    public void packetSent(){
        this.numberOfPackets++;
        this.numberOfPacketsSent++;
    }

    public void packetRetransmit(){
        numberOfPacketsSent++;
        numberOfPacketsRetransmitted++;
    }

    public void packetLost(){
        numberOfPacketsLost++;
    }

    public void packetReceived(Packet packetReceived){
        numberOfPacketsReceived++;
    }

    public int getNumberOfPackets() {
        return numberOfPackets;
    }

    public int getNumberOfPacketsSent() {
        return numberOfPacketsSent;
    }

    public int getNumberOfPacketsRetransmitted() {
        return numberOfPacketsRetransmitted;
    }

    public int getNumberOfPacketsLost() {
        return numberOfPacketsLost;
    }

    public int getNumberOfPacketsReceived() {
        return numberOfPacketsReceived;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Statistics{");
        sb.append("\n");

        //Number
        sb.append("    ");
        sb.append("Number: ");
        sb.append("\n");

        sb.append("    ");
        sb.append("    ");
        sb.append("Number of packets: ");
        sb.append(numberOfPackets);
        sb.append("\n");

        sb.append("    ");
        sb.append("    ");
        sb.append("Number of packets sent: ");
        sb.append(numberOfPacketsSent);
        sb.append("\n");

        sb.append("    ");
        sb.append("    ");
        sb.append("Number og packets lost: ");
        sb.append(numberOfPacketsLost);
        sb.append("\n");

        sb.append("    ");
        sb.append("    ");
        sb.append("number of packets retransmitted: ");
        sb.append(numberOfPacketsRetransmitted);
        sb.append("\n");

        sb.append("    ");
        sb.append("    ");
        sb.append("number of packets received: ");
        sb.append(numberOfPacketsReceived);
        sb.append("\n");



        sb.append('}');
        return sb.toString();
    }
}
