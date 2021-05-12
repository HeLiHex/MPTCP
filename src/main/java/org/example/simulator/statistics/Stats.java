package org.example.simulator.statistics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.data.Packet;
import org.example.util.Util;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class Stats {

    protected static final String DIR = "./charts/";
    private static final int timescale = 1000;

    // Arrival
    protected final ArrayList<Packet> arrivalPacket = new ArrayList<>();
    protected final ArrayList<Double> arrivalTime = new ArrayList<>();
    protected final ArrayList<Integer> arrivalNum = new ArrayList<>();

    // Departure
    protected final ArrayList<Packet> departurePacket = new ArrayList<>();
    protected final ArrayList<Double> departureTime = new ArrayList<>();
    protected final ArrayList<Integer> departureNum = new ArrayList<>();

    // Inter arrival times
    protected final ArrayList<Double> interArrivalTimes = new ArrayList<>();

    // Time in system
    protected final ArrayList<Double> timeInSystem = new ArrayList<>();

    // number of packets in system
    protected final ArrayList<Integer> numPacketsInSystem = new ArrayList<>();

    private double meanArrivalRate;
    private double meanTimeInSystem;
    private double meanNumPacketsInSystem;

    protected abstract String fileName();

    protected abstract void additionalCalculations();

    private void doCalculations(){
        if (this.interArrivalTimes.isEmpty()) return;
        this.meanArrivalRate = this.arrivalNum.size()/(this.arrivalTime.get(this.arrivalTime.size()-1));

        this.meanTimeInSystem = this.timeInSystem.stream().mapToDouble(f -> f.doubleValue()).average().getAsDouble();

        //Little's law
        // L = 1/E[A] * W
        this.meanNumPacketsInSystem = this.meanArrivalRate * this.meanTimeInSystem;
    }

    public void packetArrival(Packet packet) {
        this.arrivalPacket.add(packet);
        this.arrivalNum.add(this.arrivalNum.size());
        this.arrivalTime.add((double) Util.seeTime()/timescale);

        // number of packets in system
        this.addPacketsInSystem();

        // inter arrival time calculation
        this.addInterArrivalTime();
    }

    public void packetDeparture(Packet packet) {
        this.departurePacket.add(packet);
        this.departureNum.add(this.arrivalNum.size());
        this.departureTime.add((double) Util.seeTime()/timescale);

        // number of packets in system
        this.removePacketsInSystem();

        // Time in system calculation
        this.addTimeInSystem(packet);
    }

    private void addTimeInSystem(Packet packet){
        int packetArrivalIndex = this.arrivalIndexOf(packet);
        int packetDepartureIndex = this.departureIndexOf(packet);

        double arrivalTime = this.arrivalTime.get(packetArrivalIndex);
        double departureTime = this.departureTime.get(packetDepartureIndex);
        double timeInSystem = departureTime - arrivalTime;
        if (timeInSystem < 0) throw new IllegalStateException("packet cannot be in system a negative amount of time");
        this.timeInSystem.add(timeInSystem);
    }

    private void addInterArrivalTime(){
        int n = this.arrivalNum.size() - 1;
        if (n > 1){
            double interArrivalTime = this.arrivalTime.get(n) - this.arrivalTime.get(n - 1);
            if (interArrivalTime < 0) throw new IllegalStateException("interarrival time is less then 0");
            this.interArrivalTimes.add(interArrivalTime);
        }else{
            this.interArrivalTimes.add(0.0);
        }
    }

    private void addPacketsInSystem(){
        if (this.numPacketsInSystem.isEmpty()){
            this.numPacketsInSystem.add(1);
        }else{
            this.numPacketsInSystem.add(this.numPacketsInSystem.get(this.numPacketsInSystem.size()-1) + 1);
        }
    }

    private void removePacketsInSystem(){
        if (!this.numPacketsInSystem.isEmpty()) {
            this.numPacketsInSystem.add(this.numPacketsInSystem.get(this.numPacketsInSystem.size() - 1) - 1);
        }
    }

    private int arrivalIndexOf(Packet packet){
        return this.arrivalPacket.indexOf(packet);
    }

    private int departureIndexOf(Packet packet){
        return this.departurePacket.indexOf(packet);
    }


    public void createArrivalChart() {
        if (this.arrivalTime.isEmpty() || this.arrivalNum.isEmpty()) return;
        if (this.arrivalTime.size() != this.arrivalNum.size()) throw new IllegalStateException("the arrays must be of equal length");

        XYChart chart = new XYChartBuilder().width(1000).height(600).xAxisTitle("Packet Arrival-Time").yAxisTitle("Packet").title("Packet Arrivals").build();
        chart.addSeries("Packet Arrivals", this.arrivalTime, this.arrivalNum);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
        saveChart(chart, "ArrivalChart_");
    }

    public void createDepartureChart() {
        if (this.departureTime.isEmpty() || this.departureNum.isEmpty()) return;
        if (this.departureTime.size() != this.departureNum.size()) throw new IllegalStateException("the arrays must be of equal length");

        XYChart chart = new XYChartBuilder().width(1000).height(600).xAxisTitle("Packet Departure-Time").yAxisTitle("Packet").title("Packet Departures").build();
        chart.addSeries("Packet Departures", this.departureTime, this.departureNum);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
        saveChart(chart,"DepartureChart_");
    }

    public void createInterArrivalChart() {
        XYChart chart = new XYChartBuilder().width(10000).height(600).xAxisTitle("Arrival Time").yAxisTitle("Interarrival Time").title("Packet Interarrival-Time").build();
        chart.addSeries("Interarrival Times", this.arrivalTime, this.interArrivalTimes);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
        saveChart(chart, "InterarrivalTime_");
    }

    public void createTimeInSystemChart() {
        XYChart chart = new XYChartBuilder().width(10000).height(600).xAxisTitle("Departure Time").yAxisTitle("Time In System").title("Packet Time In System").build();
        chart.addSeries("Time in system", this.departureTime, this.timeInSystem);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
        saveChart(chart, "TimeInSystem_");
    }

    public void createNumberOfPacketsInSystemChart() {
        XYChart chart = new XYChartBuilder().width(10000).height(600).xAxisTitle("Time").yAxisTitle("Number of packets in system").title("number of packets in system").build();
        chart.addSeries("Packets in system", this.combine(this.arrivalTime, this.departureTime), this.numPacketsInSystem).setMarker(SeriesMarkers.DIAMOND);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
        saveChart(chart, "PacketsInSystem_");
    }

    private List<Double> combine(List<Double> l1, List<Double> l2){
        List<Double> result = new ArrayList<>();
        result.addAll(l1);
        result.addAll(l2);
        Collections.sort(result);
        return result;
    }

    public double getMeanArrivalRate() {
        return meanArrivalRate;
    }

    public double getMeanTimeInSystem() {
        return meanTimeInSystem;
    }

    public double getMeanNumPacketsInSystem() {
        return meanNumPacketsInSystem;
    }


    public void saveChart(XYChart chart, String chartName){
        try {
            BitmapEncoder.saveBitmap(chart, DIR + chartName + fileName(), BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            Logger.getLogger("").log(Level.WARNING, "lol");
        }
    }

    @Override
    public String toString() {
        this.doCalculations();
        this.additionalCalculations();
        var mapper = new ObjectMapper();
        try {
            String formattedString = mapper.writeValueAsString(this)
                    .replace("{", "{\n      ")
                    .replace("}", "\n}")
                    .replace(",", ",\n      ");
            return this.fileName() + " " + formattedString;
        } catch (JsonProcessingException e) {
            return "fail";
        }
    }
}
