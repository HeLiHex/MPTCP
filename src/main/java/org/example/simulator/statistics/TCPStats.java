package org.example.simulator.statistics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.network.address.Address;
import org.example.util.Util;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.internal.series.Series;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Scatter;

public class TCPStats {

    private final String filename;
    private final String dir;
    private long rtt;

    // CWND
    private final ArrayList<Integer> congestionWindowCapacities = new ArrayList<>();
    private final ArrayList<Double> congestionWindowTime = new ArrayList<>();

    // Arrival
    private final ArrayList<Double> arrivalTime = new ArrayList<>();
    private final ArrayList<Integer> arrivalCustomer = new ArrayList<>();

    // Departure
    private final ArrayList<Double> departureTime = new ArrayList<>();
    private final ArrayList<Integer> departureCustomer = new ArrayList<>();

    private int numberOfPacketsSent; //total number of packets sent (both normal and retransmissions)
    private int numberOfPacketsRetransmitted; //total number of packets retransmitted
    private int numberOfPacketsFastRetransmitted; // total number of packets dropped
    private int numberOfPacketsArrived; //total number of packets that are enqueued
    private int numberOfAcksReceived; //total number of ACKs received
    private int numberOfPacketsReceived; //total number of packets received

    private double goodput;

    public TCPStats(Address address) {
        this.dir = "./charts/";
        this.filename = address.toString();
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

    public void packetArrival() {
        this.numberOfPacketsArrived++;
        this.arrivalCustomer.add(this.arrivalCustomer.size());
        this.arrivalTime.add((double) Util.seeTime());
    }

    public void packetDeparture() {
        this.numberOfPacketsReceived++;
        this.departureCustomer.add(this.arrivalCustomer.size());
        this.departureTime.add((double) Util.seeTime());
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

    private void setGoodput(){
        this.goodput = (double)(this.numberOfPacketsSent + this.numberOfPacketsRetransmitted + this.numberOfPacketsFastRetransmitted)/((double)Util.seeTime());
    }

    public void trackCwnd(int cwnd) {
        congestionWindowCapacities.add(cwnd);
        congestionWindowTime.add((double) Util.seeTime()/(double) this.rtt);
    }

    public void createCWNDChart() {
        if (this.congestionWindowTime.isEmpty() || this.congestionWindowCapacities.isEmpty()) return;
        if (this.congestionWindowTime.size() != this.congestionWindowCapacities.size()) throw new IllegalStateException("the arrays must be of equal length");

        XYChart chart = new XYChartBuilder().width(10000).height(500).xAxisTitle("congestionWindowTime").yAxisTitle("CWND").title("Congestion Window Capacity").build();
        chart.addSeries("CWND", congestionWindowTime, congestionWindowCapacities);
        try {
            BitmapEncoder.saveBitmap(chart, this.dir + "CWNDChart_" + this.filename, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            Logger.getLogger("").log(Level.WARNING, "lol");
        }
    }

    public void createArrivalChart() {
        if (this.arrivalTime.isEmpty() || this.arrivalCustomer.isEmpty()) return;
        if (this.arrivalTime.size() != this.arrivalCustomer.size()) throw new IllegalStateException("the arrays must be of equal length");

        XYChart chart = new XYChartBuilder().width(800).height(600).xAxisTitle("Packet Arrival-Time").yAxisTitle("Packet").title("Packet Arrivals").build();
        chart.addSeries("Packet Arrivals", this.arrivalTime, this.arrivalCustomer);
        chart.getStyler().setDefaultSeriesRenderStyle(Scatter);
        try {
            BitmapEncoder.saveBitmap(chart, this.dir + "ArrivalChart_" + this.filename, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            Logger.getLogger("").log(Level.WARNING, "lol");
        }
    }

    public void createDepartureChart() {
        if (this.departureTime.isEmpty() || this.departureCustomer.isEmpty()) return;
        if (this.departureTime.size() != this.departureCustomer.size()) throw new IllegalStateException("the arrays must be of equal length");

        XYChart chart = new XYChartBuilder().width(800).height(600).xAxisTitle("Packet Departure-Time").yAxisTitle("Packet").title("Packet Departures").build();
        chart.addSeries("Packet Departures", this.departureTime, this.departureCustomer);
        chart.getStyler().setDefaultSeriesRenderStyle(Scatter);
        try {
            BitmapEncoder.saveBitmap(chart, this.dir + "DepartureChart_" + this.filename, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            Logger.getLogger("").log(Level.WARNING, "lol");
        }
    }

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
