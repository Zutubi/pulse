package com.zutubi.pulse.web.project;

import com.zutubi.pulse.charting.*;
import com.zutubi.pulse.charting.demo.DemoDataSourceFactory;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.pulse.web.ActionSupport;

import java.util.Map;
import java.util.TreeMap;

/**
 * <class comment/>
 */
public class ViewReportsAction extends ActionSupport
{
    private long id;
    private Project project;
    private ProjectManager projectManager;

    private Map buildResultsChart;
    private Map buildTimesChart;

    private int timeframe = 45;

    private BuildResultDao buildResultDao;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public int getTimeframe()
    {
        return timeframe;
    }

    public void setTimeframe(int timeframe)
    {
        this.timeframe = timeframe;
    }

    public Project getProject()
    {
        return project;
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
        project = projectManager.getProject(id);

        DBBuildResultsDataSource dataSource = new DBBuildResultsDataSource();
        dataSource.setProject(project);
        dataSource.setBuildResultDao(buildResultDao);

        BuildResultsChart chart = new BuildResultsChart();
        chart.setTimeframe(timeframe);
        chart.setSource(dataSource);
        this.buildResultsChart = ChartUtils.renderForWeb(chart.render(), 400, 300);

        BuildTimesChart btChart = new BuildTimesChart();
        btChart.setRange(45);
        btChart.setSource(dataSource);
        this.buildTimesChart = ChartUtils.renderForWeb(btChart.render(), 400, 300);

        return SUCCESS;
    }

    public Map getBuildResultsChart()
    {
        return buildResultsChart;
    }

    public Map getBuildTimesChart()
    {
        return buildTimesChart;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildResultDao(BuildResultDao buildResultDao)
    {
        this.buildResultDao = buildResultDao;
    }
}
