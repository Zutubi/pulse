package com.zutubi.pulse.master.charting;

import com.zutubi.i18n.Messages;
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

import java.util.Date;

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

    private TimeBasedChartData data;

    private static final Messages I18N = Messages.getInstance(BuildResultsChart.class);

    private boolean hasResults;

    public BuildResultsChart()
    {
    }

    public void setData(TimeBasedChartData data)
    {
        this.data = data;
    }

    public JFreeChart render()
    {
        TimeTableXYDataset ds = generateDataSet();
        
        final XYItemRenderer renderer = new StackedXYBarRenderer();
        renderer.setSeriesPaint(0, ChartColours.FAILURE);
        renderer.setSeriesPaint(1, ChartColours.SUCCESS);
        renderer.setToolTipGenerator(this);

        final DateAxis domainAxis = new DateAxis(I18N.format(dateAxisLabel));
        final ValueAxis rangeAxis = new NumberAxis(I18N.format(rangeAxisLabel));

        // If the dataset has no results, then we need to specify a lower bound of zero otherwise the
        // rendered graph will have the range axis in the middle of the page. We do not want to be
        // showing the negative range.  If there is a better way to achive this outcome, please use it.
        if (!hasResults)
        {
            rangeAxis.setLowerBound(0D);
        }

        final XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);

        final JFreeChart chart = new JFreeChart(I18N.format(chartTitle), plot);

        chart.setBackgroundPaint(ChartColours.BACKGROUND);
        chart.setBorderVisible(false);

        return chart;
    }

    private TimeTableXYDataset generateDataSet()
    {
        final String failureSeries = I18N.format(failureSeriesLabel);
        final String successSeries = I18N.format(successSeriesLabel);

        final TimeTableXYDataset ds = new TimeTableXYDataset();
        data.populateDataSet(ds, new DailyDataHandler()
        {
            public void handle(Date date, DailyData data)
            {
                if(data == null)
                {
                    ds.add(new Day(date), 0, failureSeries);
                    ds.add(new Day(date), 0, successSeries);
                }
                else
                {
                    ds.add(new Day(date), data.getFailureCount(), failureSeries);
                    ds.add(new Day(date), data.getSuccessCount(), successSeries);
                    hasResults = true;
                }
            }
        });

        return ds;
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
            return String.format(I18N.format(failureSeriesTooltip), p);
        }
        else
        {
            return String.format(I18N.format(successSeriesTooltip), p);
        }
    }
}
