package com.zutubi.pulse.master.charting.render;

import com.zutubi.pulse.master.charting.model.DataPoint;
import com.zutubi.pulse.master.charting.model.ReportData;
import com.zutubi.pulse.master.charting.model.SeriesData;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.tove.config.project.reports.CustomFieldSource;
import com.zutubi.pulse.master.tove.config.project.reports.ReportConfiguration;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeTableXYDataset;

import java.util.Date;
import java.util.List;

/**
 * A chart that reports over days by aggregating builds on the same day.
 */
public class ByDayChart extends CustomChart
{
    public ByDayChart(ReportConfiguration configuration, List<BuildResult> builds, CustomFieldSource customFieldSource)
    {
        super(configuration, builds, customFieldSource);
    }

    @Override
    protected ValueAxis createDomainAxis()
    {
        return new DateAxis(getConfiguration().getDomainUnits().getLabel());
    }

    protected TimeTableXYDataset generateDataSet(ReportData reportData)
    {
        TimeTableXYDataset dataset = new TimeTableXYDataset();
        for (SeriesData seriesData: reportData.getSeriesList())
        {
            for (DataPoint point: seriesData.getPoints())
            {
                dataset.add(new Day(new Date(point.getX())), point.getY(), seriesData.getName(), false);
            }
        }

        return dataset;
    }
}
