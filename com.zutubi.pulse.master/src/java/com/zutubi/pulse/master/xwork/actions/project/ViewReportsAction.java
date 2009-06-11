package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.charting.build.DefaultCustomFieldSource;
import com.zutubi.pulse.master.charting.render.ByBuildChart;
import com.zutubi.pulse.master.charting.render.ByDayChart;
import com.zutubi.pulse.master.charting.render.Chart;
import com.zutubi.pulse.master.charting.render.ChartUtils;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.tove.config.project.reports.*;
import com.zutubi.util.TextUtils;

import java.io.IOException;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Action for viewing project reports (e.g. build time trend graph).
 */
public class ViewReportsAction extends ProjectActionBase
{
    private static final int DEFAULT_TIME_FRAME = 45;

    private String group;
    private int timeframe = DEFAULT_TIME_FRAME;
    private String timeunit = null;

    private List<String> groupNames;
    private int buildCount;
    private List<Map> reports = new LinkedList<Map>();

    private BuildResultDao buildResultDao;
    private MasterConfigurationManager configurationManager;

    public String getGroup()
    {
        return group;
    }

    public void setGroup(String group)
    {
        this.group = group;
    }

    public int getTimeframe()
    {
        return timeframe;
    }

    public void setTimeframe(int timeframe)
    {
        this.timeframe = timeframe;
    }

    public String getTimeunit()
    {
        return timeunit;
    }

    public void setTimeunit(String timeunit)
    {
        this.timeunit = timeunit;
    }

    public List<String> getGroupNames()
    {
        return groupNames;
    }

    public int getBuildCount()
    {
        return buildCount;
    }

    public List<Map> getReports()
    {
        return reports;
    }

    public String doInput() throws Exception
    {
        return execute();
    }

    public String execute() throws Exception
    {
        Project project = getRequiredProject();

        Map<String, ReportGroupConfiguration> reportGroups = project.getConfig().getReportGroups();
        if (reportGroups.size() > 0)
        {
            groupNames = new LinkedList<String>(reportGroups.keySet());
            if (!TextUtils.stringSet(group))
            {
                group = groupNames.get(0);
            }

            ReportGroupConfiguration config = reportGroups.get(group);
            if (config == null)
            {
                throw new IllegalArgumentException("Unknown report group '" + group + "'");
            }

            if (timeunit == null)
            {
                // Time period not specified by form POST, use default.
                timeframe = config.getDefaultTimeFrame();
                timeunit = config.getDefaultTimeUnit().name().toLowerCase();
            }

            List<BuildResult> builds = getBuilds(project);
            buildCount = builds.size();
            if (buildCount > 0)
            {
                CustomFieldSource fieldSource = new DefaultCustomFieldSource(configurationManager.getDataDirectory());
                for (ReportConfiguration report: config.getReports().values())
                {
                    reports.add(render(report, builds, fieldSource));
                }
            }
        }

        return SUCCESS;
    }

    private List<BuildResult> getBuilds(Project project)
    {
        if (timeframe <= 0)
        {
            // Ignore nonsense timeframes by applying default.
            timeframe = DEFAULT_TIME_FRAME;
        }

        if (ReportTimeUnit.BUILDS.toString().equalsIgnoreCase(timeunit))
        {
            return buildResultDao.findLatestCompleted(project, 0, timeframe);
        }
        else
        {
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DAY_OF_YEAR, -timeframe);
            cal.set(Calendar.HOUR, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            return buildResultDao.findSinceByProject(project, cal.getTime());
        }
    }

    private Map render(ReportConfiguration config, List<BuildResult> builds, CustomFieldSource customFieldSource) throws IOException
    {
        Chart chart;
        if (config.getDomainUnits() == DomainUnit.BUILD_IDS)
        {
            chart = new ByBuildChart(config, builds, customFieldSource);
        }
        else
        {
            chart = new ByDayChart(config, builds, customFieldSource);
        }

        return ChartUtils.renderForWeb(chart.render(), config.getWidth(), config.getHeight());
    }

    public void setBuildResultDao(BuildResultDao buildResultDao)
    {
        this.buildResultDao = buildResultDao;
    }

    public void setConfigurationManager(MasterConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
