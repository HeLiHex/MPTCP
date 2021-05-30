package org.example.simulator.statistics;

import org.example.data.Packet;
import org.example.network.address.Address;
import org.example.util.Util;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.util.ArrayList;

public class TCPStats extends Stats {

    private final String filename;
    private final String dir;
    // CWND
    private final ArrayList<Integer> congestionWindowCapacities = new ArrayList<>();
    private final ArrayList<Double> congestionWindowTime = new ArrayList<>();
    private long rtt;
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
    protected void additionalCalculations() {
        this.setGoodput();
        this.setLossRate();
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
    public void packetArrival(Packet packet) {
        this.numberOfPacketsArrived++;
        super.packetArrival(packet);
    }

    @Override
    public void packetDeparture(Packet packet) {
        this.numberOfPacketsReceived++;
        super.packetDeparture(packet);
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

    private void setGoodput() {
        if (congestionWindowTime.isEmpty()) return;
        this.goodput = (double) this.numberOfPacketsSent / (this.congestionWindowTime.get(this.congestionWindowTime.size() - 1) / (double) TIMESCALE);
    }

    private void setLossRate() {
        if (this.numberOfPacketsSent == 0) {
            this.lossRate = 0;
            return;
        }

        this.lossRate = (this.numberOfPacketsFastRetransmitted + this.numberOfPacketsRetransmitted) / (double) this.numberOfPacketsSent;
    }

    public void trackCwnd(int cwnd) {
        congestionWindowCapacities.add(cwnd);
        congestionWindowTime.add((double) Util.seeTime());
    }

    public void createCWNDChart() {
        if (this.congestionWindowTime.isEmpty() || this.congestionWindowCapacities.isEmpty()) return;
        if (this.congestionWindowTime.size() != this.congestionWindowCapacities.size())
            throw new IllegalStateException("the arrays must be of equal length");

        XYChart chart = new XYChartBuilder()
                .width(CHART_WIDTH)
                .height(CHART_HEIGHT)
                .xAxisTitle("Time")
                .yAxisTitle("CWND Size")
                .title("Congestion Window Capacity")
                .theme(theme)
                .build();
        chart.getStyler().setChartTitleFont(TITLE_FONT);
        chart.getStyler().setAxisTitleFont(AXIS_FONT);
        chart.getStyler().setAxisTickLabelsFont(TICK_FONT);
        chart.getStyler().setLegendVisible(false);
        chart.getStyler().setXAxisLabelRotation(45);
        chart.addSeries("CWND", congestionWindowTime, congestionWindowCapacities).setMarker(SeriesMarkers.NONE);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Area);
        this.saveChart(chart, "CWNDChart_");
    }
}
