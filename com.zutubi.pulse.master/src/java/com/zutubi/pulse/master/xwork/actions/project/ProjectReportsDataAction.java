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

import java.util.List;
import java.util.Map;

/**
 * An action that provides the JSON data for rendering a project reports page.
 */
public class ProjectReportsDataAction extends ProjectReportsAction
{
    private BuildResultDao buildResultDao;
    private MasterConfigurationManager configurationManager;

    public String execute() throws Exception
    {
        // Parent implementation determines the input parameters.
        super.execute();

        Project project = getRequiredProject();
        Map<String, ReportGroupConfiguration> reportGroups = project.getConfig().getReportGroups();
        if (reportGroups.size() > 0)
        {
            List<BuildResult> builds = ChartUtils.getBuilds(project, timeFrame, convertUnits(timeUnit), buildResultDao);
            int buildCount = builds.size();
            model = new ProjectReportsModel(group, groupNames, timeFrame, timeUnit, buildCount);
            if (buildCount > 0)
            {
                CustomFieldSource fieldSource = new DefaultCustomFieldSource(configurationManager.getDataDirectory());
                for (ReportConfiguration report: reportGroups.get(group).getReports().values())
                {
                    model.addReport(new ProjectReportsModel.ReportModel(ChartUtils.renderForWeb(report, builds, fieldSource)));
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