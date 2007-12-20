package com.zutubi.pulse.bootstrap.tasks;

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
        List<Project> projects = projectManager.getProjects(true);
        for (Project project : projects)
        {
            List<BuildResult> abortedResults = buildManager.abortUnfinishedBuilds(project, ABORT_MESSAGE);
            for(BuildResult result: abortedResults)
            {
                projectManager.buildCompleted(project.getId(), false);
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
