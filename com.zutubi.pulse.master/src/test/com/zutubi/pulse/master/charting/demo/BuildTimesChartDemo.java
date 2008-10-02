package com.zutubi.pulse.master.charting.demo;

import com.zutubi.pulse.master.charting.BuildResultsDataSource;
import com.zutubi.pulse.master.charting.BuildTimesChart;
import com.zutubi.pulse.master.charting.TimeBasedChartData;

import java.io.IOException;

/**
 * <class comment/>
 */
public class BuildTimesChartDemo extends ChartDemoSupport
{
    public static void main(final String[] args) throws IOException
    {
        final BuildTimesChart chart = new BuildTimesChart(false, false);
        BuildResultsDataSource source = DemoDataSourceFactory.createBuildResultsDataSource();
        TimeBasedChartData data = new TimeBasedChartData();
        data.setTimeframe(35);
        data.setSource(source);
        chart.setData(data);
        new BuildResultsChartDemo().displayChart(chart.render());
    }
}
