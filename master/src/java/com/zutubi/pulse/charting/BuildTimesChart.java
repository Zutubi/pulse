package com.zutubi.pulse.charting;

import com.zutubi.pulse.core.model.ResultState;
import com.zutubi.pulse.i18n.Messages;
import com.zutubi.pulse.util.Constants;
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

    private static final Messages I18N = Messages.getInstance(BuildTimesChart.class);

    public String generateToolTip(XYDataset dataset, int series, int item)
    {
        return String.format(I18N.format(seriesTimeTooltip), dataset.getY(series, item));
    }

    public void setRange(int range)
    {
        this.range = range;
    }

    public JFreeChart render()
    {
        CategoryTableXYDataset ds = new CategoryTableXYDataset();

        long firstNumber = -1, lastNumber = -1;

        long minTime = Long.MAX_VALUE;
        long maxTime = Long.MIN_VALUE;

        BuildResultsResultSet results = source.getLastByBuilds(range);

        List<ChartData> data = new LinkedList<ChartData>();

        while (results.next())
        {
            long number = results.getNumber();
            if (results.getState() != ResultState.SUCCESS)
            {
                // only count successes.
                continue;
            }

            if (firstNumber == -1)
            {
                firstNumber = number;
            }
            lastNumber = number;
            
            long elapsed = results.getElapsed() / Constants.SECOND; // convert to seconds. 

            data.add(new ChartData(number, elapsed));

            // track the maximum and minimum values.
            minTime = Math.min(minTime, elapsed);
            maxTime = Math.max(maxTime, elapsed);
        }

        for (ChartData d : data)
        {
            ds.add(d.number, d.time, I18N.format(seriesTimeLabel));
        }

        if (lastNumber - firstNumber < range)
        {
            lastNumber = firstNumber + range;
        }

        XYItemRenderer renderer = new DefaultXYItemRenderer();
        renderer.setToolTipGenerator(this);

        NumberAxis domainAxis = new NumberAxis(I18N.format(domainAxisLabel));
        domainAxis.setLowerBound(firstNumber - 1);
        domainAxis.setUpperBound(lastNumber + 1);

        if (minTime == Long.MAX_VALUE && maxTime == Long.MIN_VALUE)
        {
            minTime = 5;
            maxTime = 100;
        }

        ValueAxis rangeAxis = new NumberAxis(I18N.format(rangeAxisLabel));
        rangeAxis.setLowerBound(Math.min(minTime, 0));
        rangeAxis.setUpperBound(maxTime + 5);

        XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);

        JFreeChart chart = new JFreeChart(I18N.format(chartLabel), plot);
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
            this.number = id;
            this.time = time;
        }

        long number;
        long time;
    }
}
