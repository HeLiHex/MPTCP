package org.example.simulator.statistics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.network.address.Address;
import org.example.util.Util;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RealTCPStats implements TCPStats {

    private final String filename;
    private long rtt;

    private final ArrayList<Integer> congestionWindowCapacities = new ArrayList<>();
    private final ArrayList<Double> time = new ArrayList<>();

    private int numberOfPacketsSent; //total number of packets sent (both normal and retransmissions)
    private int numberOfPacketsRetransmitted; //total number of packets retransmitted
    private int numberOfPacketsFastRetransmitted; // total number of packets dropped
    private int numberOfAcksReceived; //total number of packets received. Should be the same as numberOfPackets(!?)

    private double goodput;

    public RealTCPStats(Address address) {
        this.filename = "./charts/TCP_" + address.toString();
    }

    public void setRtt(long rtt) {
        this.rtt = rtt;
    }

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
    public void ackReceived() {
        this.numberOfAcksReceived++;
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
    public int getNumberOfPacketsFastRetransmitted() {
        return this.numberOfPacketsFastRetransmitted;
    }

    @Override
    public int getNumberOfAcksReceived() {
        return this.numberOfAcksReceived;
    }

    @Override
    public double getGoodput() {
        return goodput;
    }

    @Override
    public void trackCwnd(int cwnd) {
        congestionWindowCapacities.add(cwnd);
        time.add((double) Util.seeTime()/(double) this.rtt);
    }

    @Override
    public void createCWNDChart() {
        if (this.time.isEmpty() || this.congestionWindowCapacities.isEmpty()) return;
        if (this.time.size() != this.congestionWindowCapacities.size()) throw new IllegalStateException("the arrays must be of equal length");

        XYChart chart = new XYChartBuilder().width(10000).height(500).xAxisTitle("time").yAxisTitle("CWND").title("Congestion Window Capacity").build();
        chart.addSeries("CWND", time, congestionWindowCapacities);
        try {
            BitmapEncoder.saveBitmap(chart, this.filename, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            Logger.getLogger("").log(Level.WARNING, "lol");
        }
    }

    private void setGoodput(){
        System.out.println(this.rtt);
        this.goodput = (double)(this.numberOfPacketsSent + this.numberOfPacketsRetransmitted + this.numberOfPacketsFastRetransmitted)/((double)Util.seeTime());
    }

    @Override
    public String toString() {
        this.setGoodput();
        var mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "fail";
        }
    }
}
