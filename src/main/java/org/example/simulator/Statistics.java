package org.example.simulator;

import org.example.util.Util;
import org.knowm.xchart.*;

import java.io.IOException;
import java.util.ArrayList;

public class Statistics {

    private static ArrayList<Integer> congestionWindowCapacities = new ArrayList<>();
    private static ArrayList<Long> time = new ArrayList<>();
    private static int numberOfPackets; //total number of packets to be served. not counting retransmissions
    private static int numberOfPacketsSent; //total number of packets sent (both normal and retransmissions)
    private static int numberOfPacketsRetransmitted; //total number of packets retransmitted
    private static int numberOfPacketsLost; //total number of packets lost
    private static int numberOfPacketsDropped; // total number of packets dropped
    private static int numberOfPacketsAckedMoreThanOnce; // total number of packets dropped
    private static int numberOfPacketsReceived; //total number of packets received. Should be the same as numberOfPackets(!?)

    public static void reset() {
        numberOfPackets = 0;
        numberOfPacketsSent = 0;
        numberOfPacketsRetransmitted = 0;
        numberOfPacketsLost = 0;
        numberOfPacketsDropped = 0;
        numberOfPacketsAckedMoreThanOnce = 0;
        numberOfPacketsReceived = 0;
    }

    public static void packetSent() {
        numberOfPackets++;
        numberOfPacketsSent++;
    }

    public static void packetRetransmit() {
        numberOfPacketsSent++;
        numberOfPacketsRetransmitted++;
    }

    public static void packetLost() {
        numberOfPacketsLost++;
    }

    public static void trackCwnd(int cwnd){
        congestionWindowCapacities.add(cwnd);
        time.add(Util.seeTime());
    }

    public static void packetDropped() {
        numberOfPacketsDropped++;
    }

    public static void packetReceived() {
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

    public static int getNumberOfPacketsDropped() {
        return numberOfPacketsDropped;
    }

    public static int getNumberOfPacketsAckedMoreThanOnce() {
        return numberOfPacketsAckedMoreThanOnce;
    }

    public static int getNumberOfPacketsReceived() {
        return numberOfPacketsReceived;
    }

    public void createChart(){
        XYChart chart = new XYChartBuilder().width(1500).height(500).xAxisTitle("time").yAxisTitle("CWND").title("Congestion Window Capacity").build();
        chart.addSeries("CWND", time, congestionWindowCapacities);
        //XYChart chart = QuickChart.getChart("Sample Chart", "X", "Y", "y(x)", time, congestionWindowCapacities);
        try {
            BitmapEncoder.saveBitmap(chart, "./Sample_Chart", BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Show it
        //new SwingWrapper(chart).displayChart();
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
        sb.append("Number of packets lost: ");
        sb.append(numberOfPacketsLost);
        sb.append("\n");

        sb.append("    ");
        sb.append("    ");
        sb.append("Number of packets dropped: ");
        sb.append(numberOfPacketsDropped);
        sb.append("\n");

        sb.append("    ");
        sb.append("    ");
        sb.append("Number of packets acked more than once: ");
        sb.append(numberOfPacketsAckedMoreThanOnce);
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
