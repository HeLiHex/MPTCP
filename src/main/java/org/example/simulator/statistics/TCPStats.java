package org.example.simulator.statistics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.network.address.Address;
import org.example.util.Util;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Scatter;

public class TCPStats extends Stats {

    private final String filename;
    private final String dir;
    private long rtt;

    // CWND
    private final ArrayList<Integer> congestionWindowCapacities = new ArrayList<>();
    private final ArrayList<Double> congestionWindowTime = new ArrayList<>();

    private int numberOfPacketsSent; //total number of packets sent (both normal and retransmissions)
    private int numberOfPacketsRetransmitted; //total number of packets retransmitted
    private int numberOfPacketsFastRetransmitted; // total number of packets dropped
    private int numberOfPacketsArrived; //total number of packets that are enqueued
    private int numberOfAcksReceived; //total number of ACKs received
    private int numberOfPacketsReceived; //total number of packets received

    private double goodput;
    private double lossRate;

    public TCPStats(Address address) {
        this.dir = "./charts/";
        this.filename = address.toString();
    }

    @Override
    protected String fileName() {
        return this.filename;
    }

    public void setRtt(long rtt) {
        this.rtt = rtt;
    }

    public void packetSend() {
        this.numberOfPacketsSent++;
    }

    public void packetRetransmit() {
        this.numberOfPacketsRetransmitted++;
    }

    public void packetFastRetransmit() {
        this.numberOfPacketsFastRetransmitted++;
    }

    @Override
    public void packetArrival() {
        this.numberOfPacketsArrived++;
        super.packetArrival();
    }

    @Override
    public void packetDeparture() {
        this.numberOfPacketsReceived++;
        super.packetDeparture();
    }

    public void ackReceived() {
        this.numberOfAcksReceived++;
    }

    public int getNumberOfPacketsSent() {
        return this.numberOfPacketsSent;
    }

    public int getNumberOfPacketsRetransmitted() {
        return this.numberOfPacketsRetransmitted;
    }

    public int getNumberOfPacketsFastRetransmitted() {
        return this.numberOfPacketsFastRetransmitted;
    }

    public int getNumberOfAcksReceived() {
        return this.numberOfAcksReceived;
    }

    public int getNumberOfPacketsArrived() {
        return numberOfPacketsArrived;
    }

    public int getNumberOfPacketsReceived() {
        return numberOfPacketsReceived;
    }

    public double getGoodput() {
        return goodput;
    }

    public double getLossRate() {
        return lossRate;
    }

    private void setGoodput(){
        this.goodput = (double)this.numberOfPacketsSent/((double)Util.seeTime());
    }

    private void setLossRate(){
        if (this.numberOfPacketsSent == 0){
            this.lossRate = 0;
            return;
        }
        this.lossRate = ((double) this.numberOfPacketsFastRetransmitted + (double) this.numberOfPacketsFastRetransmitted)/(double) this.numberOfPacketsSent;
    }

    public void trackCwnd(int cwnd) {
        congestionWindowCapacities.add(cwnd);
        congestionWindowTime.add((double) Util.seeTime()/(double) this.rtt);
    }

    public void createCWNDChart() {
        if (this.congestionWindowTime.isEmpty() || this.congestionWindowCapacities.isEmpty()) return;
        if (this.congestionWindowTime.size() != this.congestionWindowCapacities.size()) throw new IllegalStateException("the arrays must be of equal length");

        XYChart chart = new XYChartBuilder().width(10000).height(500).xAxisTitle("congestionWindowTime").yAxisTitle("CWND").title("Congestion Window Capacity").build();
        chart.addSeries("CWND", congestionWindowTime, congestionWindowCapacities).setMarker(SeriesMarkers.NONE);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area);
        try {
            BitmapEncoder.saveBitmap(chart, this.dir + "CWNDChart_" + this.filename, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            Logger.getLogger("").log(Level.WARNING, "lol");
        }
    }

    public String toString() {
        this.setGoodput();
        this.setLossRate();
        this.doCalculations();
        var mapper = new ObjectMapper();
        try {
            String formattedString = mapper.writeValueAsString(this)
                    .replace("{", "{\n      ")
                    .replace("}", "\n}")
                    .replace(",", ",\n      ");
            return this.filename + " " + formattedString;
        } catch (JsonProcessingException e) {
            return "fail";
        }
    }
}
