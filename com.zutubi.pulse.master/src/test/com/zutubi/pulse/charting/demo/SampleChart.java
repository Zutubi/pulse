package com.zutubi.pulse.charting.demo;

import org.jfree.chart.labels.CategoryToolTipGenerator;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.category.CategoryDataset;

import java.util.Calendar;
import java.awt.*;

/**
 * A bar graph of build results over time.  The success/failure status of the builds are represented
 * by the colour of the bar.
 * <p/>
 * The time interval for each bar is configurable, as is the time period represented on the final chart.
 */
public class SampleChart implements CategoryToolTipGenerator
{
    private String chartTitle = "Build results";

    private String xlabel = "Time interval";

    private String ylabel = "Number of builds";

    private double[][] data;

    public SampleChart()
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

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (int r = 0; r < data.length; r++) {
            String rowKey = "row " + (r + 1);
            Calendar today = Calendar.getInstance();
            for (int c = 0; c < data[r].length; c++) {
                String columnKey = "col " + (c+1);
                dataset.addValue((Number)new Double(data[r][c]), rowKey, columnKey);
                today.add(Calendar.DAY_OF_YEAR, 1);
            }
        }

        JFreeChart chart = ChartFactory.createStackedBarChart(chartTitle, xlabel, ylabel, dataset, PlotOrientation.VERTICAL, legend, tooltips, urls);
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        // series 0, Category: Failed. series 1, Category: Passed.
        chart.getCategoryPlot().getRenderer().setSeriesPaint(0, Color.RED);
        chart.getCategoryPlot().getRenderer().setSeriesPaint(1, Color.GREEN);

        chart.getCategoryPlot().getRenderer().setToolTipGenerator(this);

        return chart;
    }

    public String generateToolTip(CategoryDataset dataset, int row, int column)
    {
        if (row >= data.length || column >= data[row].length)
        {
            return "Unknown";
        }

        double v = data[row][column];
        double t = data[0][column] + data[1][column];

        long p = Math.round(((t != 0) ? v/t : 0) * 100);
        if (row == 0)
        {
            return String.format("%s%s failed", p, "%");
        }
        else
        {
            return String.format("%s%s passed", p, "%");
        }
    }
}
