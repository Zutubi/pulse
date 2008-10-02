package com.zutubi.pulse.web.project;

import com.zutubi.pulse.master.charting.*;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;

import java.util.Map;
import java.util.TreeMap;

/**
 * Action for viewing project reports (e.g. build time trend graph).
 */
public class ViewReportsAction extends ProjectActionBase
{
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;

    private Map buildResultsChart;
    private Map testCountChart;
    private Map buildTimesChart;
    private Map stageTimesChart;

    private int timeframe = 45;
    private boolean zoom = false;

    private BuildResultDao buildResultDao;

    public int getTimeframe()
    {
        return timeframe;
    }

    public void setTimeframe(int timeframe)
    {
        this.timeframe = timeframe;
    }

    public boolean isZoom()
    {
        return zoom;
    }

    public void setZoom(boolean zoom)
    {
        this.zoom = zoom;
    }

    public String doInput() throws Exception
    {
        return execute();
    }

    public Map getTimeframes()
    {
        Map<Integer, String> timeframes = new TreeMap<Integer, String>();
        timeframes.put(15, "15");
        timeframes.put(30, "30");
        timeframes.put(45, "45");
        timeframes.put(90, "90");
        return timeframes;
    }

    public String execute() throws Exception
    {
        Project project = getRequiredProject();
        
        DBBuildResultsDataSource dataSource = new DBBuildResultsDataSource();
        dataSource.setProject(project);
        dataSource.setBuildResultDao(buildResultDao);

        TimeBasedChartData chartData = new TimeBasedChartData();
        chartData.setSource(dataSource);
        chartData.setTimeframe(timeframe);

        BuildResultsChart chart = new BuildResultsChart();
        chart.setData(chartData);
        this.buildResultsChart = ChartUtils.renderForWeb(chart.render(), WIDTH, HEIGHT);

        BuildTimesChart btChart = new BuildTimesChart(false, zoom);
        btChart.setData(chartData);
        this.buildTimesChart = ChartUtils.renderForWeb(btChart.render(), WIDTH, HEIGHT);

        BuildTimesChart stChart = new BuildTimesChart(true, zoom);
        stChart.setData(chartData);
        this.stageTimesChart = ChartUtils.renderForWeb(stChart.render(), WIDTH, HEIGHT);

        TestCountChart tcChart = new TestCountChart(zoom);
        tcChart.setData(chartData);
        this.testCountChart = ChartUtils.renderForWeb(tcChart.render(), WIDTH, HEIGHT);

        return SUCCESS;
    }

    public Map getBuildResultsChart()
    {
        return buildResultsChart;
    }

    public Map getTestCountChart()
    {
        return testCountChart;
    }

    public Map getBuildTimesChart()
    {
        return buildTimesChart;
    }

    public Map getStageTimesChart()
    {
        return stageTimesChart;
    }

    public void setBuildResultDao(BuildResultDao buildResultDao)
    {
        this.buildResultDao = buildResultDao;
    }
}
