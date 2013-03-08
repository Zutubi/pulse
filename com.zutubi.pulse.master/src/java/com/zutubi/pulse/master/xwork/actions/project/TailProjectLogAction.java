package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.project.ProjectLogger;
import com.zutubi.pulse.master.project.ProjectLoggerManager;

import java.io.IOException;

public class TailProjectLogAction extends TailBuildLogAction
{
    private ProjectLoggerManager projectLoggerManager;

    public String execute() throws Exception
    {
        initialiseProperties();

        Project project = getProject();

        ProjectLogger logger = projectLoggerManager.getLogger(project.getId());

        this.logExists = true;

        if (raw)
        {
            try
            {
                inputStream = logger.getInput();
                return "raw";
            }
            catch (IOException e)
            {
                addActionError("Unable to open project log: " + e.getMessage());
                return ERROR;
            }
        }
        else
        {
            this.tail = logger.tail(maxLines);
            return "tail";
        }
    }

    public void setProjectLoggerManager(ProjectLoggerManager projectLoggerManager)
    {
        this.projectLoggerManager = projectLoggerManager;
    }
}
