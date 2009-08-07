package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.bootstrap.MasterConfigurationManager;
import com.zutubi.pulse.master.charting.build.DefaultCustomFieldSource;
import com.zutubi.pulse.master.charting.render.ChartUtils;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.persistence.BuildResultDao;
import com.zutubi.pulse.master.tove.config.project.reports.CustomFieldSource;
import com.zutubi.pulse.master.tove.config.project.reports.ReportConfiguration;
import com.zutubi.pulse.master.tove.config.project.reports.ReportGroupConfiguration;
import com.zutubi.pulse.master.tove.config.project.reports.ReportTimeUnit;
import com.zutubi.util.StringUtils;

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
            if (!StringUtils.stringSet(group))
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

            if (timeframe <= 0)
            {
                // Ignore nonsense timeframes by applying default.
                timeframe = DEFAULT_TIME_FRAME;
            }

            List<BuildResult> builds = ChartUtils.getBuilds(project, timeframe, convertUnits(timeunit), buildResultDao);
            buildCount = builds.size();
            if (buildCount > 0)
            {
                CustomFieldSource fieldSource = new DefaultCustomFieldSource(configurationManager.getDataDirectory());
                for (ReportConfiguration report: config.getReports().values())
                {
                    reports.add(ChartUtils.renderForWeb(report, builds, fieldSource));
                }
            }
        }

        return SUCCESS;
    }

    private ReportTimeUnit convertUnits(String timeunit)
    {
        if (ReportTimeUnit.BUILDS.toString().equalsIgnoreCase(timeunit))
        {
            return ReportTimeUnit.BUILDS;
        }
        else
        {
            return ReportTimeUnit.DAYS;
        }
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
