package com.zutubi.pulse.charting;

import com.zutubi.pulse.core.model.ResultState;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StackedXYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.XYDataset;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * A bar graph of build results over time.  The success/failure status of the builds are represented
 * by the colour of the bar.
 * <p/>
 * The time interval for each bar is configurable, as is the time period represented on the final chart.
 */
public class BuildResultsChart implements XYToolTipGenerator, Chart
{
    private String chartTitle = "chart.label";
    private String dateAxisLabel = "axis.domain.label";
    private String rangeAxisLabel = "axis.range.label";

    private String failureSeriesLabel = "series.failure.label";
    private String successSeriesLabel = "series.success.label";

    private String failureSeriesTooltip = "series.failure.tooltip";
    private String successSeriesTooltip = "series.success.tooltip";

    private BuildResultsDataSource source;

    /**
     * The default timeframe is 30 days.
     */
    public static final int DEFAULT_TIMEFRAME = 30;

    /**
     * The timeframe of this chart represents the number of days that will make up the domain of this chart.
     *
     * @see BuildResultsChart#DEFAULT_TIMEFRAME
     */
    private int timeframe = DEFAULT_TIMEFRAME;

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("d-MMM-yyyy");
    private static final Calendar CALENDAR = Calendar.getInstance();

    public BuildResultsChart()
    {
    }

    /**
     * Get the timeframe for this chart.
     *
     * @return the timeframe in days.
     */
    public int getTimeframe()
    {
        return timeframe;
    }

    /**
     * Set the timeframe for this chart.
     *
     * @param timeframe in days.
     */
    public void setTimeframe(int timeframe)
    {
        this.timeframe = timeframe;
    }

    public void setSource(BuildResultsDataSource source)
    {
        this.source = source;
    }

    public JFreeChart render()
    {
        // process data source: group by day of year and count the successes and failures.
        Map<String, ChartData> map = aggregateData(source);

        TimeTableXYDataset ds = generateDataSet(map);

        final XYItemRenderer renderer = new StackedXYBarRenderer();
        renderer.setSeriesPaint(0, ChartColors.FAILURE);
        renderer.setSeriesPaint(1, ChartColors.SUCCESS);
        renderer.setToolTipGenerator(this);

        final DateAxis domainAxis = new DateAxis(dateAxisLabel);
        final ValueAxis rangeAxis = new NumberAxis(rangeAxisLabel);

        final XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);

        final JFreeChart chart = new JFreeChart(chartTitle, plot);

        chart.setBackgroundPaint(ChartColors.BACKGROUND);
        chart.setBorderVisible(false);

        return chart;
    }

    private TimeTableXYDataset generateDataSet(Map<String, ChartData> map)
    {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -timeframe);
        
        TimeTableXYDataset ds = new TimeTableXYDataset();
        for (int i = 0; i < timeframe; i++)
        {
            Date date = cal.getTime();
            String key = getAggregateKey(date.getTime());
            if (map.containsKey(key))
            {
                ChartData d = map.get(key);
                ds.add(new Day(date), d.failureCount, failureSeriesLabel);
                ds.add(new Day(date), d.successCount, successSeriesLabel);
            }
            else
            {
                ds.add(new Day(date), 0, failureSeriesLabel);
                ds.add(new Day(date), 0, successSeriesLabel);
            }
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return ds;
    }

    private Map<String, ChartData> aggregateData(BuildResultsDataSource source)
    {
        BuildResultsResultSet resultSet = source.getLastByDays(timeframe);

        Map<String, ChartData> map = new TreeMap<String, ChartData>();
        while (resultSet.next())
        {
            String timestamp = getAggregateKey(resultSet.getEndTime());
            if (!map.containsKey(timestamp))
            {
                map.put(timestamp, new ChartData());
            }

            ChartData a = map.get(timestamp);
            ResultState state = resultSet.getState();
            if (state == ResultState.SUCCESS)
            {
                a.successCount++;
            }
            else
            {
                a.failureCount++;
            }
        }
        return map;
    }

    /**
     * Determine the tool tip to be displayed for the specified item in the speicfied series.
     */
    public String generateToolTip(XYDataset dataset, int series, int item)
    {
        double value = dataset.getYValue(series, item);
        double total = dataset.getYValue(0, item) + dataset.getYValue(1, item);

        long p = Math.round(((total != 0) ? value / total : 0) * 100);
        if (series == 0)
        {
            return String.format(failureSeriesTooltip, p);
        }
        else
        {
            return String.format(successSeriesTooltip, p);
        }
    }

    protected String getAggregateKey(long l)
    {
        CALENDAR.setTimeInMillis(l);
        return DATE_FMT.format(CALENDAR.getTime());
    }

    private class ChartData
    {
        int successCount;
        int failureCount;
    }
}
