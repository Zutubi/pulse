package com.zutubi.pulse.master.charting.render;

import com.zutubi.pulse.master.charting.model.DataPoint;
import com.zutubi.pulse.master.charting.model.ReportData;
import com.zutubi.pulse.master.charting.model.SeriesData;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.project.reports.CustomFieldSource;
import com.zutubi.pulse.master.tove.config.project.reports.ReportConfiguration;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYSeries;

import java.util.List;

/**
 * A chart that reports over individual builds.
 */
public class ByBuildChart extends CustomChart
{
    public ByBuildChart(ReportConfiguration configuration, List<BuildResult> builds, CustomFieldSource customFieldSource)
    {
        super(configuration, builds, customFieldSource);
    }

    @Override
    protected ValueAxis createDomainAxis()
    {
        NumberAxis axis = new NumberAxis(getConfiguration().getDomainUnits().getLabel());
        axis.setAutoRangeIncludesZero(false);
        axis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        return axis;
    }

    protected DefaultTableXYDataset generateDataSet(ReportData reportData)
    {
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        for (SeriesData seriesData: reportData.getSeriesList())
        {
            XYSeries series = new XYSeries(seriesData.getName(), false, false);
            for (DataPoint point: seriesData.getPoints())
            {
                series.add(point.getX(), point.getY());
            }
            dataset.addSeries(series);
        }

        return dataset;
    }
}