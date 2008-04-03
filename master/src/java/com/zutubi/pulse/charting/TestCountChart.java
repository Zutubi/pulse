package com.zutubi.pulse.charting;

import com.zutubi.pulse.i18n.Messages;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.DefaultXYItemRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.util.Date;

/**
 * <class comment/>
 */
public class TestCountChart implements XYToolTipGenerator, Chart
{
    private static final Messages I18N = Messages.getInstance(TestCountChart.class);

    private boolean zoom;
    private TimeBasedChartData data;

    private String domainAxisLabel = "axis.domain.label";
    private String rangeAxisLabel = "axis.range.label";
    private String chartLabel = "chart.label";
    private String seriesTimeLabel = "series.time.label";
    private String seriesTimeTooltip = "series.time.tooltip";


    public TestCountChart(boolean zoom)
    {
        this.zoom = zoom;
    }

    public String generateToolTip(XYDataset dataset, int series, int item)
    {
        return String.format(I18N.format(seriesTimeTooltip), dataset.getY(series, item));
    }

    public JFreeChart render()
    {
        final TimeTableXYDataset ds = new TimeTableXYDataset();

        final double[] minCount = new double[]{Double.MAX_VALUE};
        final double[] maxCount = new double[]{Double.MIN_VALUE};

        data.populateDataSet(ds, new DailyDataHandler()
        {
            public void handle(Date day, DailyData data)
            {
                if(data != null && data.getSuccessCount() > 0)
                {
                    double testCount = data.getAverageTestCount();
                    ds.add(new Day(day), testCount, I18N.format(seriesTimeLabel));
                    minCount[0] = Math.min(minCount[0], testCount);
                    maxCount[0] = Math.max(maxCount[0], testCount);
                }
            }
        });

        XYItemRenderer renderer = new DefaultXYItemRenderer();
        renderer.setToolTipGenerator(this);

        DateAxis domainAxis = new DateAxis(I18N.format(domainAxisLabel));
        domainAxis.setLowerBound(data.getLowerBound());
        domainAxis.setUpperBound(data.getUpperBound());

        if (minCount[0] == Double.MAX_VALUE && maxCount[0] == Double.MIN_VALUE)
        {
            minCount[0] = 5;
            maxCount[0] = 100;
        }

        NumberAxis rangeAxis = new NumberAxis(I18N.format(rangeAxisLabel));
        if (zoom)
        {
            double buffer = Math.max(maxCount[0] / 15.0, 5);
            rangeAxis.setLowerBound(Math.max(minCount[0] - buffer, 0));
            rangeAxis.setUpperBound(maxCount[0] + buffer);
        }

        XYPlot plot = new XYPlot(ds, domainAxis, rangeAxis, renderer);

        JFreeChart chart = new JFreeChart(I18N.format(chartLabel), plot);
        chart.setBackgroundPaint(Color.WHITE);
        chart.setBorderVisible(false);

        chart.getXYPlot().getRenderer().setSeriesPaint(0, ChartColours.NEUTRAL);

        return chart;
    }

    public void setData(TimeBasedChartData data)
    {
        this.data = data;
    }
}
