package com.zutubi.pulse;

import com.zutubi.pulse.model.*;

import java.util.List;

/**
 */
public class BuildAborter implements Runnable
{
    private ProjectManager projectManager;
    private BuildManager buildManager;
    private UserManager userManager;
    private static final String ABORT_MESSAGE = "Server shut down while build in progress";

    public void run()
    {
        List<Project> projects = projectManager.getAllProjects();
        for (Project project : projects)
        {
            List<BuildResult> abortedResults = buildManager.abortUnfinishedBuilds(project, ABORT_MESSAGE);
            for(BuildResult result: abortedResults)
            {
                projectManager.buildCompleted(project.getId(), result.getSpecName(), false);
            }
        }

        List<User> users = userManager.getAllUsers();
        for(User user: users)
        {
            buildManager.abortUnfinishedBuilds(user, ABORT_MESSAGE);
        }
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
