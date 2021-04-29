package org.example.simulator.statistics;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Chart {

    private final String chartName;
    private final String xAxisName;
    private final String yAxisName;
    private final int height;
    private final int width;
    private final List<Integer> x;
    private final List<Double> y;

    public Chart(String chartName) {
        this(chartName, "x-axis", "y-axis");
    }

    public Chart(String chartName, String xAxisName, String yAxisName) {
        this(chartName, xAxisName, yAxisName, 500, 10000);
    }

    public Chart(String chartName, String xAxisName, String yAxisName, int width, int height) {
        this.chartName = chartName;
        this.xAxisName = xAxisName;
        this.yAxisName = yAxisName;
        this.height = height;
        this.width = width;
        this.x = new ArrayList<>();
        this.y = new ArrayList<>();
    }

    public void createCWNDChart() {
        if (this.x.isEmpty() || this.y.isEmpty()) return;
        if (this.x.size() != this.y.size()) throw new IllegalStateException("the arrays must be of equal length");

        XYChart chart = new XYChartBuilder().width(10000).height(500).xAxisTitle(this.xAxisName).yAxisTitle(this.yAxisName).title(this.chartName).build();
        chart.addSeries(chartName, this.x, this.y);
        try {
            BitmapEncoder.saveBitmap(chart, "chart", BitmapEncoder.BitmapFormat.PNG);
        } catch (IOException e) {
            Logger.getLogger("").log(Level.WARNING, "lol");
        }
    }
}
