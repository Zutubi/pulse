package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;

import java.util.List;

/**
 * Aborts incomplete builds found on startup.
 */
public class BuildAborterStartupTask implements StartupTask
{
    private static final String ABORT_MESSAGE = "Server shut down while build in progress";

    private ProjectManager projectManager;
    private BuildManager buildManager;
    private UserManager userManager;

    public void execute()
    {
        buildManager.executeInTransaction(new Runnable()
        {
            public void run()
            {
                List<Project> projects = projectManager.getProjects(true);
                for (Project project : projects)
                {
                    projectManager.abortUnfinishedBuilds(project, ABORT_MESSAGE);
                }

                List<User> users = userManager.getAllUsers();
                for (User user: users)
                {
                    buildManager.abortUnfinishedBuilds(user, ABORT_MESSAGE);
                }
            }
        });
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
