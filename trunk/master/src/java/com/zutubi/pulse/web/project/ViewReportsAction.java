package com.zutubi.pulse.web.project;

import com.zutubi.pulse.charting.*;
import com.zutubi.pulse.model.BuildSpecification;
import com.zutubi.pulse.model.NamedEntityComparator;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.persistence.BuildResultDao;
import com.zutubi.pulse.web.ActionSupport;

import java.util.*;

/**
 * <class comment/>
 */
public class ViewReportsAction extends ActionSupport
{
    private static final int WIDTH = 400;
    private static final int HEIGHT = 300;

    private long id;
    private Project project;
    private long specId = 0;
    private ProjectManager projectManager;
    private Map<Long, String> buildSpecifications;

    private Map buildResultsChart;
    private Map testCountChart;
    private Map buildTimesChart;
    private Map stageTimesChart;

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

    public long getSpecId()
    {
        return specId;
    }

    public void setSpecId(long specId)
    {
        this.specId = specId;
    }

    public Map<Long, String> getBuildSpecifications()
    {
        if(buildSpecifications == null)
        {
            buildSpecifications = new LinkedHashMap<Long, String>();
            List<BuildSpecification> specs = project.getBuildSpecifications();
            Collections.sort(specs, new NamedEntityComparator());
            for(BuildSpecification s: specs)
            {
                buildSpecifications.put(s.getId(), s.getName());
            }
        }

        return buildSpecifications;
    }

    public String execute() throws Exception
    {
        project = projectManager.getProject(id);
        if(project == null)
        {
            addActionError("Unknown project [" + id + "]");
            return ERROR;
        }
        
        BuildSpecification spec = null;
        if(specId > 0)
        {
            spec = project.getBuildSpecification(specId);
        }

        if(spec == null)
        {
            spec = project.getBuildSpecifications().get(0);
            specId = spec.getId();
        }

        DBBuildResultsDataSource dataSource = new DBBuildResultsDataSource();
        dataSource.setProject(project);
        dataSource.setSpec(spec);
        dataSource.setBuildResultDao(buildResultDao);

        TimeBasedChartData chartData = new TimeBasedChartData();
        chartData.setSource(dataSource);
        chartData.setTimeframe(timeframe);

        BuildResultsChart chart = new BuildResultsChart();
        chart.setData(chartData);
        this.buildResultsChart = ChartUtils.renderForWeb(chart.render(), WIDTH, HEIGHT);

        BuildTimesChart btChart = new BuildTimesChart(false);
        btChart.setData(chartData);
        this.buildTimesChart = ChartUtils.renderForWeb(btChart.render(), WIDTH, HEIGHT);

        BuildTimesChart stChart = new BuildTimesChart(true);
        stChart.setData(chartData);
        this.stageTimesChart = ChartUtils.renderForWeb(stChart.render(), WIDTH, HEIGHT);

        TestCountChart tcChart = new TestCountChart();
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

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildResultDao(BuildResultDao buildResultDao)
    {
        this.buildResultDao = buildResultDao;
    }
}
