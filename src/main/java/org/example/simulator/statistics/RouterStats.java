package org.example.simulator.statistics;

import org.example.network.Router;
import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RouterStats extends Stats {

    private final Router router;
    private final ArrayList queueSize;

    public RouterStats(Router router) {
        this.router = router;
        this.queueSize = new ArrayList();
    }

    public void trackQueueSize(int queueSize) {
        this.queueSize.add(queueSize);
    }

    public void createQueueSizeChart() {
        XYChart chart = new XYChartBuilder().width(10000).height(600).xAxisTitle("Time").yAxisTitle("Queue size").title("Queue size").build();
        chart.addSeries("Router queue size", this.queueSize).setMarker(SeriesMarkers.NONE);
        chart.getStyler().setDefaultSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Line);
        try {
            BitmapEncoder.saveBitmap(chart, DIR + "QueueSize_" + this.fileName(), BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            Logger.getLogger("").log(Level.WARNING, "lol");
        }
    }

    @Override
    protected void additionalCalculations() {
        //no additional calculations
    }

    @Override
    protected String fileName() {
        return router.toString();
    }
}
