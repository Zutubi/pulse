package com.zutubi.pulse;

import com.zutubi.pulse.bootstrap.StartupTask;
import com.zutubi.pulse.model.*;

import java.util.List;

/**
 */
public class BuildAborterStartupTask implements StartupTask
{
    private ProjectManager projectManager;
    private BuildManager buildManager;
    private UserManager userManager;
    private static final String ABORT_MESSAGE = "Server shut down while build in progress";

    public void execute()
    {
        List<Project> projects = projectManager.getNameToConfig();
        for (Project project : projects)
        {
            buildManager.abortUnfinishedBuilds(project, ABORT_MESSAGE);
            if(project.getState() == Project.State.BUILDING || project.getState() == Project.State.PAUSING)
            {
                projectManager.buildCompleted(project.getId());
            }
        }

        List<User> users = userManager.getAllUsers();
        for(User user: users)
        {
            buildManager.abortUnfinishedBuilds(user, ABORT_MESSAGE);
        }
    }

    public boolean haltOnFailure()
    {
        return false;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
