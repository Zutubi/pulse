package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.reports.ReportGroupConfiguration;
import com.zutubi.util.StringUtils;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Action for viewing project reports (e.g. build time trend graph).
 */
public class ProjectReportsAction extends ProjectActionBase
{
    private static final Messages I18N = Messages.getInstance(ProjectReportsAction.class);
    
    private static final int DEFAULT_TIME_FRAME = 45;

    protected ProjectReportsModel model;

    protected String group;
    protected int timeFrame = DEFAULT_TIME_FRAME;
    protected String timeUnit = null;
    protected List<String> groupNames;

    public void setGroup(String group)
    {
        this.group = group;
    }

    public void setTimeFrame(int timeFrame)
    {
        this.timeFrame = timeFrame;
    }

    public void setTimeUnit(String timeUnit)
    {
        this.timeUnit = timeUnit;
    }

    public ProjectReportsModel getModel()
    {
        return model;
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
                throw new IllegalArgumentException(I18N.format("unknown.group", group));
            }

            if (timeUnit == null)
            {
                // Time period not specified by form POST, use default.
                timeFrame = config.getDefaultTimeFrame();
                timeUnit = config.getDefaultTimeUnit().name().toLowerCase();
            }

            if (timeFrame <= 0)
            {
                // Ignore nonsense timeframes by applying default.
                timeFrame = DEFAULT_TIME_FRAME;
            }
        }

        model = new ProjectReportsModel(group, groupNames, timeFrame, timeUnit, 0);

        return SUCCESS;
    }
}
