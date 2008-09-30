package com.zutubi.pulse.charting.demo;

import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.StackedXYAreaRenderer2;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.time.Day;
import org.jfree.data.xy.XYDataset;

import java.util.Calendar;
import java.awt.*;

/**
 * A bar graph of build results over time.  The success/failure status of the builds are represented
 * by the colour of the bar.
 * <p/>
 * The time interval for each bar is configurable, as is the time period represented on the final chart.
 */
public class SampleAreaChart implements XYToolTipGenerator
{
    private String chartTitle = "Build results";

    private String xlabel = "Time interval";

    private String ylabel = "Number of builds";

    private double[][] data;

    public SampleAreaChart()
    {
    }

    public JFreeChart render()
    {
        boolean legend = true;
        boolean tooltips = true;
        boolean urls = false;

        // each data point represents a aggregate build result for a particular date range.

        // render 15 days of results.

        data = new double[][]
            {{3.0, 7.0, 2.0, 8.0, 1.0, 0.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0},      // Category: Failed
             {10.0, 4.0, 15.0, 14.0, 5.0, 73.0, 7.0, 4.0, 6.0, 3.0, 0.0, 0.0, 0.0, 0.0, 0.0}};

        TimeTableXYDataset dataset = new TimeTableXYDataset();

        for (int r = 0; r < data.length; r++) {
            String rowKey = "row " + (r + 1);
            Calendar today = Calendar.getInstance();
            for (int c = 0; c < data[r].length; c++) {
                dataset.add(new Day(today.getTime()), data[r][c], rowKey );
                today.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        JFreeChart chart = ChartFactory.createStackedXYAreaChart(chartTitle, xlabel, ylabel, dataset, PlotOrientation.VERTICAL, legend, tooltips, urls);
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        XYPlot xyPlot = chart.getXYPlot();
        XYItemRenderer renderer = new StackedXYAreaRenderer2();
        renderer.setSeriesPaint(0, Color.RED);
        renderer.setSeriesOutlinePaint(0, Color.BLUE);
        renderer.setSeriesPaint(1, Color.GREEN);
        renderer.setSeriesOutlinePaint(1, Color.BLACK);
        renderer.setToolTipGenerator(this);
        xyPlot.setRenderer(renderer);

        xyPlot.setDomainAxis(new DateAxis());

        // series 0, Category: Failed. series 1, Category: Passed.
/*
        chart.getXYPlot().getRenderer().setSeriesPaint(0, Color.RED);
        chart.getXYPlot().getRenderer().setSeriesPaint(1, Color.GREEN);
*/

        chart.getXYPlot().getRenderer().setToolTipGenerator(this);

        return chart;
    }


    public String generateToolTip(XYDataset dataset, int series, int item)
    {
        if (series >= data.length || item >= data[series].length)
        {
            return "Unknown";
        }

        double v = data[series][item];
        double t = data[0][item] + data[1][item];

        long p = Math.round(((t != 0) ? v/t : 0) * 100);
        if (series == 0)
        {
            return String.format("%s%s failed", p, "%");
        }
        else
        {
            return String.format("%s%s passed", p, "%");
        }
    }
}
