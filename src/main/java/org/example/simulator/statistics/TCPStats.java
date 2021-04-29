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

    // Inter arrival times
    private final ArrayList<Double> interArrivalTimes = new ArrayList<>();

    // Time in system
    private final ArrayList<Double> timeInSystem = new ArrayList<>();

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

    private void findInterArrivalTimes(){
        for (int i = 1; i < this.arrivalTime.size(); i++) {
            if (this.arrivalTime.get(i) - this.arrivalTime.get(i-1) < 0) throw new IllegalStateException("interarrival time is less then 0");
            this.interArrivalTimes.add(this.arrivalTime.get(i) - this.arrivalTime.get(i-1));
        }
    }

    private void findTimeInSystem(){
        for (int i = 0; i < this.departureTime.size(); i++) {
            if (this.departureTime.get(i) - this.arrivalTime.get(i) < 0) throw new IllegalStateException("wait is less then 0");
            this.timeInSystem.add(this.departureTime.get(i) - this.arrivalTime.get(i));
        }
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

    public void createInterArrivalChart() {
        this.findInterArrivalTimes();
        XYChart chart = new XYChartBuilder().width(10000).height(600).xAxisTitle("Packet").yAxisTitle("Interarrival Time").title("Packet Interarrival-Time").build();
        chart.addSeries("Interarrival Times", this.interArrivalTimes);
        chart.getStyler().setDefaultSeriesRenderStyle(Scatter);
        try {
            BitmapEncoder.saveBitmap(chart, this.dir + "InterarrivalTime_" + this.filename, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            Logger.getLogger("").log(Level.WARNING, "lol");
        }
    }

    public void createTimeInSystemChart() {
        this.findTimeInSystem();
        XYChart chart = new XYChartBuilder().width(10000).height(600).xAxisTitle("Packet").yAxisTitle("Time In System").title("Packet Time In System").build();
        chart.addSeries("Time in system", this.timeInSystem);
        chart.getStyler().setDefaultSeriesRenderStyle(Scatter);
        try {
            BitmapEncoder.saveBitmap(chart, this.dir + "TimeInSystem_" + this.filename, BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            Logger.getLogger("").log(Level.WARNING, "lol");
        }
    }

    public String toString() {
        this.setGoodput();
        this.setLossRate();
        var mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "fail";
        }
    }
}
