package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.project.ProjectLogger;
import com.zutubi.pulse.master.project.ProjectLoggerManager;

public class TailProjectLogAction extends TailRecipeLogAction
{
    private ProjectLoggerManager projectLoggerManager;

    public String execute() throws Exception
    {
        initialiseProperties();

        Project project = getProject();

        ProjectLogger logger = projectLoggerManager.getLogger(project.getId());

        this.tail = logger.tail(maxLines);
        this.logExists = true;
        return "tail";
    }

    public void setProjectLoggerManager(ProjectLoggerManager projectLoggerManager)
    {
        this.projectLoggerManager = projectLoggerManager;
    }
}
