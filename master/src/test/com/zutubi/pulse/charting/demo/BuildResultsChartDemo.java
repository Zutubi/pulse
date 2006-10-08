package com.zutubi.pulse.charting.demo;

import com.zutubi.pulse.charting.BuildResultsChart;

import java.io.IOException;

/**
 * <class comment/>
 */
public class BuildResultsChartDemo extends ChartDemoSupport
{
    public static void main(final String[] args) throws IOException
    {
        final BuildResultsChart chart = new BuildResultsChart();
        chart.setTimeframe(35);
        chart.setSource(DemoDataSourceFactory.createBuildResultsDataSource());
        new BuildResultsChartDemo().displayChart(chart.render());
    }
}
