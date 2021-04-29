package org.example.simulator.statistics;

import org.example.util.Util;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.knowm.xchart.XYSeries.XYSeriesRenderStyle.Scatter;

public abstract class Stats {

    private static final String DIR = "./charts/";

    // Arrival
    protected final ArrayList<Double> arrivalTime = new ArrayList<>();
    protected final ArrayList<Integer> arrivalCustomer = new ArrayList<>();

    // Departure
    protected final ArrayList<Double> departureTime = new ArrayList<>();
    protected final ArrayList<Integer> departureCustomer = new ArrayList<>();

    // Inter arrival times
    protected final ArrayList<Double> interArrivalTimes = new ArrayList<>();

    // Time in system
    protected final ArrayList<Double> timeInSystem = new ArrayList<>();


    protected abstract String fileName();

    public void packetArrival() {
        int numberOfPackers = this.arrivalCustomer.size();
        this.arrivalCustomer.add(numberOfPackers);
        this.arrivalTime.add((double) Util.seeTime());

        // inter arrival time calculation
        if (numberOfPackers > 0){
            if (this.arrivalTime.get(numberOfPackers) - this.arrivalTime.get(numberOfPackers-1) < 0) throw new IllegalStateException("interarrival time is less then 0");
            this.interArrivalTimes.add(this.arrivalTime.get(numberOfPackers) - this.arrivalTime.get(numberOfPackers-1));
        }
    }

    public void packetDeparture() {
        this.departureCustomer.add(this.arrivalCustomer.size());
        this.departureTime.add((double) Util.seeTime());
    }

    private void findTimeInSystem(){
        for (int i = 0; i < this.departureTime.size(); i++) {
            if (this.departureTime.get(i) - this.arrivalTime.get(i) < 0) throw new IllegalStateException("wait is less then 0");
            this.timeInSystem.add(this.departureTime.get(i) - this.arrivalTime.get(i));
        }
    }

    public void createArrivalChart() {
        if (this.arrivalTime.isEmpty() || this.arrivalCustomer.isEmpty()) return;
        if (this.arrivalTime.size() != this.arrivalCustomer.size()) throw new IllegalStateException("the arrays must be of equal length");

        XYChart chart = new XYChartBuilder().width(800).height(600).xAxisTitle("Packet Arrival-Time").yAxisTitle("Packet").title("Packet Arrivals").build();
        chart.addSeries("Packet Arrivals", this.arrivalTime, this.arrivalCustomer);
        chart.getStyler().setDefaultSeriesRenderStyle(Scatter);
        try {
            BitmapEncoder.saveBitmap(chart, DIR + "ArrivalChart_" + this.fileName(), BitmapEncoder.BitmapFormat.PNG);
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
            BitmapEncoder.saveBitmap(chart, DIR + "DepartureChart_" + this.fileName(), BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            Logger.getLogger("").log(Level.WARNING, "lol");
        }
    }

    public void createInterArrivalChart() {
        XYChart chart = new XYChartBuilder().width(10000).height(600).xAxisTitle("Packet").yAxisTitle("Interarrival Time").title("Packet Interarrival-Time").build();
        chart.addSeries("Interarrival Times", this.interArrivalTimes);
        chart.getStyler().setDefaultSeriesRenderStyle(Scatter);
        try {
            BitmapEncoder.saveBitmap(chart, DIR + "InterarrivalTime_" + this.fileName(), BitmapEncoder.BitmapFormat.PNG);
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
            BitmapEncoder.saveBitmap(chart, DIR + "TimeInSystem_" + this.fileName(), BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            Logger.getLogger("").log(Level.WARNING, "lol");
        }
    }


}
