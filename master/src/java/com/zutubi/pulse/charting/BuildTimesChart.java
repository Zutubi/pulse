package com.zutubi.pulse.charting;

import com.zutubi.pulse.i18n.Messages;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.XYDataset;

import static java.awt.Color.WHITE;
import java.util.Date;

/**
 * <class comment/>
 */
public class BuildTimesChart implements XYToolTipGenerator, Chart
{
    private static final Messages I18N = Messages.getInstance(BuildTimesChart.class);

    private boolean stages;
    private TimeBasedChartData data;

    private String domainAxisLabel = "axis.domain.label";
    private String rangeAxisLabel = "axis.range.label";
    private String chartLabel = "chart.label";
    private String seriesTimeLabel = "series.time.label";
    private String seriesTimeTooltip = "series.time.tooltip";


    public BuildTimesChart(boolean stages)
    {
        this.stages = stages;
        if(stages)
        {
            chartLabel += ".stages";
        }
    }

    public String generateToolTip(XYDataset dataset, int series, int item)
    {
        return String.format(I18N.format(seriesTimeTooltip), dataset.getY(series, item));
    }

    public JFreeChart render()
    {
        final TimeTableXYDataset ds = new TimeTableXYDataset();

        final double[] minTime = new double[]{Double.MAX_VALUE};
        final double[] maxTime = new double[]{Double.MIN_VALUE};

        data.populateDataSet(ds, new DailyDataHandler()
        {
            public void handle(Date day, DailyData data)
            {
                if(data != null && data.getSuccessCount() > 0)
                {
                    double elapsed;

                    if(stages)
                    {
                        elapsed = data.getAverageStageTime();
                    }
                    else
                    {
                        elapsed = data.getAverageBuildTime();
                    }

                    ds.add(new Day(day), elapsed, I18N.format(seriesTimeLabel));
                    minTime[0] = Math.min(minTime[0], elapsed);
                    maxTime[0] = Math.max(maxTime[0], elapsed);
                }

            }
        });

        XYItemRenderer renderer = new DefaultXYItemRenderer();
        renderer.setToolTipGenerator(this);

        DateAxis domainAxis = new DateAxis(I18N.format(domainAxisLabel));
        domainAxis.setLowerBound(data.getLowerBound());
        domainAxis.setUpperBound(data.getUpperBound());

        if (minTime[0] == Double.MAX_VALUE && maxTime[0] == Double.MIN_VALUE)
        {
            minTime[0] = 5;
            maxTime[0] = 100;
        }

        ValueAxis rangeAxis = new NumberAxis(I18N.format(rangeAxisLabel));
        double buffer = Math.max(maxTime[0] / 15.0, 5);
        rangeAxis.setLowerBound(Math.max(minTime[0] - buffer, 0));
        rangeAxis.setUpperBound(maxTime[0] + buffer);

        XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);

        JFreeChart chart = new JFreeChart(I18N.format(chartLabel), plot);
        chart.setBackgroundPaint(WHITE);
        chart.setBorderVisible(false);

        chart.getXYPlot().getRenderer().setSeriesPaint(0, ChartColours.NEUTRAL);

        return chart;
    }

    public void setData(TimeBasedChartData data)
    {
        this.data = data;
    }
}
