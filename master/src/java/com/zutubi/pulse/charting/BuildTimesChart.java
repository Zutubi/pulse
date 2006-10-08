package com.zutubi.pulse.charting;

import com.zutubi.pulse.core.model.ResultState;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.CategoryTableXYDataset;
import org.jfree.data.xy.XYDataset;

import static java.awt.Color.BLUE;
import static java.awt.Color.WHITE;
import java.util.List;
import java.util.LinkedList;

/**
 * <class comment/>
 */
public class BuildTimesChart implements XYToolTipGenerator, Chart
{
    private BuildResultsDataSource source;

    private String domainAxisLabel = "axis.domain.label";
    private String rangeAxisLabel = "axis.range.label";
    private String chartLabel = "chart.label";
    private String seriesTimeLabel = "series.time.label";
    private String seriesTimeTooltip = "series.time.tooltip";

    public static final int DEFAULT_RANGE = 45;

    private int range = DEFAULT_RANGE;

    public String generateToolTip(XYDataset dataset, int series, int item)
    {
        return String.format(seriesTimeTooltip, dataset.getY(series, item));
    }

    public void setRange(int range)
    {
        this.range = range;
    }

    public JFreeChart render()
    {
        CategoryTableXYDataset ds = new CategoryTableXYDataset();

        long firstId = -1, lastId = -1;

        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;

        BuildResultsResultSet results = source.getLastByBuilds(range);

        List<ChartData> data = new LinkedList<ChartData>();

        while (results.next())
        {
            long id = results.getId();
            if (results.getState() != ResultState.SUCCESS)
            {
                // only count successes.
                continue;
            }

            if (firstId == -1)
            {
                firstId = id;
            }
            lastId = id;
            
            long elapsed = results.getElapsed();

            data.add(new ChartData(id, elapsed));

            // track the maximum and minimum values.
            minTime = Math.min(minTime, elapsed);
            maxTime = Math.max(maxTime, elapsed);
        }

        for (ChartData d : data)
        {
            ds.add(d.id, d.time, seriesTimeLabel);
        }

        if (lastId - firstId < range)
        {
            lastId = firstId + range;
        }

        XYItemRenderer renderer = new DefaultXYItemRenderer();
        renderer.setToolTipGenerator(this);

        NumberAxis domainAxis = new NumberAxis(domainAxisLabel);
        domainAxis.setLowerBound(firstId - 1);
        domainAxis.setUpperBound(lastId + 1);

        ValueAxis rangeAxis = new NumberAxis(rangeAxisLabel);
        rangeAxis.setLowerBound(minTime - 5);
        rangeAxis.setUpperBound(maxTime + 5);

        XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);

        JFreeChart chart = new JFreeChart(chartLabel, plot);
        chart.setBackgroundPaint(WHITE);
        chart.setBorderVisible(false);

        chart.getXYPlot().getRenderer().setSeriesPaint(0, BLUE);

        return chart;
    }

    public void setSource(BuildResultsDataSource dataSource)
    {
        this.source = dataSource;
    }

    private class ChartData
    {

        public ChartData(long id, long time)
        {
            this.id = id;
            this.time = time;
        }

        long id;
        long time;
    }
}
