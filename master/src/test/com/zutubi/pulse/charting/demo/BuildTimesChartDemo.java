package com.zutubi.pulse.charting.demo;

import com.zutubi.pulse.charting.BuildTimesChart;
import com.zutubi.pulse.charting.demo.DemoDataSourceFactory;

import java.io.IOException;

/**
 * <class comment/>
 */
public class BuildTimesChartDemo extends ChartDemoSupport
{
    public static void main(final String[] args) throws IOException
    {
        final BuildTimesChart chart = new BuildTimesChart();
        chart.setSource(DemoDataSourceFactory.createBuildResultsDataSource());
        new BuildResultsChartDemo().displayChart(chart.render());
    }
}
