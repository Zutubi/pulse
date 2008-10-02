package com.zutubi.pulse.master.charting.demo;

import com.zutubi.pulse.master.charting.BuildResultsChart;
import com.zutubi.pulse.master.charting.BuildResultsDataSource;
import com.zutubi.pulse.master.charting.TimeBasedChartData;

import java.io.IOException;

/**
 * <class comment/>
 */
public class BuildResultsChartDemo extends ChartDemoSupport
{
    public static void main(final String[] args) throws IOException
    {
        final BuildResultsChart chart = new BuildResultsChart();
        BuildResultsDataSource resultsDataSource = DemoDataSourceFactory.createBuildResultsDataSource();
        TimeBasedChartData data = new TimeBasedChartData();
        data.setTimeframe(35);
        data.setSource(resultsDataSource);
        chart.setData(data);
        new BuildResultsChartDemo().displayChart(chart.render());
    }
}
