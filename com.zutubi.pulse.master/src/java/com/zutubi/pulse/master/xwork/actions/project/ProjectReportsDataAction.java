/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import com.zutubi.pulse.master.xwork.actions.GraphModel;

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
                    model.addReport(new GraphModel(ChartUtils.renderForWeb(report, builds, fieldSource)));
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