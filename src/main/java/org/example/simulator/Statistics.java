package org.example.simulator;

import org.example.data.Packet;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

public class Statistics {

    private static final HashMap<Packet, Instant> packetsInSendingQueue = new HashMap<>();
    private static final HashMap<Packet, Instant> packetsInTransmission = new HashMap<>();
    private static final HashMap<Packet, Instant> packetsInReceivingQueue = new HashMap<>();
    private static final HashMap<Packet, Instant> packetsInSystem = new HashMap<>();

    private static long timeInSendingQueue = 0; //calculate average using numberOfPackets
    private static long timeInReceivingQueue = 0; //calculate average using numberOfPacketsReceived (I think)
    private static long timeInTransmission = 0; //calculate average using numberOfPackets.
    private static long timeInSystem = 0; //calculate average using numberOfPacketsReceived
    private static long totalTimeInSendingQueue = 0;
    private static long totalTimeInReceivingQueue = 0;
    private static long totalTimeInTransmission = 0;
    private static long totalTimeInSystem = 0;

    private static int numberOfPackets = 0; //total number of packets to be served. not counting retransmissions
    private static int numberOfPacketsSent = 0; //total number of packets sent (both normal and retransmissions)
    private static int numberOfPacketsRetransmitted = 0; //total number of packets retransmitted
    private static int numberOfPacketsLost = 0; //total number of packets lost
    private static int numberOfPacketsReceived = 0; //total number of packets received. Should be the same as numberOfPackets(!?)


    private static Instant getCurrentTime(){
        return Instant.now();
    }

    public static void packetSent(Packet packetSent){
        Instant instant = getCurrentTime();

        packetsInSendingQueue.put(packetSent, instant);
        packetsInSystem.put(packetSent, instant);

        numberOfPackets++;
        numberOfPacketsSent++;
    }

    public static void packetOutOfSendingQueue(Packet packet){
        totalTimeInSendingQueue += Duration.between(packetsInSendingQueue.get(packet), getCurrentTime()).getSeconds();
        packetsInTransmission.put(packet, getCurrentTime());
    }

    public static void packetRetransmit(){
        numberOfPacketsSent++;
        numberOfPacketsRetransmitted++;
    }

    public static void packetLost(){
        numberOfPacketsLost++;
    }

    public static void packetReceived(Packet packetReceived){
        Instant instant = getCurrentTime();
        try{
            Duration between = Duration.between(packetsInTransmission.get(packetReceived), instant);
            totalTimeInTransmission += between.getSeconds();
        }catch (NullPointerException e){}
        packetsInReceivingQueue.put(packetReceived, instant);
        totalTimeInSystem = Duration.between(packetsInSystem.get(packetReceived), instant).getSeconds();
        numberOfPacketsReceived++;
    }

    public static int getNumberOfPackets() {
        return numberOfPackets;
    }

    public static int getNumberOfPacketsSent() {
        return numberOfPacketsSent;
    }

    public static int getNumberOfPacketsRetransmitted() {
        return numberOfPacketsRetransmitted;
    }

    public static int getNumberOfPacketsLost() {
        return numberOfPacketsLost;
    }

    public static int getNumberOfPacketsReceived() {
        return numberOfPacketsReceived;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder("Statistics{");
        sb.append("\n");

        //Time
        sb.append("    ");
        sb.append("Average time: ");
        sb.append("\n");

        sb.append("    ");
        sb.append("    ");
        sb.append("Average time in sending queue: ");
        sb.append(timeInSendingQueue);
        sb.append("\n");

        sb.append("    ");
        sb.append("    ");
        sb.append("Average time in receiving queue: ");
        sb.append(timeInReceivingQueue);
        sb.append("\n");

        sb.append("    ");
        sb.append("    ");
        sb.append("Average time in transmission: ");
        sb.append(timeInTransmission);
        sb.append("\n");

        sb.append("    ");
        sb.append("    ");
        sb.append("Average time in system: ");
        sb.append(timeInSystem);
        sb.append("\n");



        //Total time
        sb.append("    ");
        sb.append("Total time: ");
        sb.append("\n");

        sb.append("    ");
        sb.append("    ");
        sb.append("Total time in sending queue: ");
        sb.append(totalTimeInSendingQueue);
        sb.append("\n");

        sb.append("    ");
        sb.append("    ");
        sb.append("Total time in receiving queue: ");
        sb.append(totalTimeInReceivingQueue);
        sb.append("\n");

        sb.append("    ");
        sb.append("    ");
        sb.append("Total time in transmission: ");
        sb.append(totalTimeInTransmission);
        sb.append("\n");

        sb.append("    ");
        sb.append("    ");
        sb.append("Total time in system: ");
        sb.append(totalTimeInSystem);
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
