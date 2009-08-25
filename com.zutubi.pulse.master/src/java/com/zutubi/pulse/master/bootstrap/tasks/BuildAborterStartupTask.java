package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.util.NullaryFunction;

import java.util.List;

/**
 */
public class BuildAborterStartupTask implements StartupTask
{
    private TransactionContext transactionContext;
    private ProjectManager projectManager;
    private BuildManager buildManager;
    private UserManager userManager;
    protected static final String ABORT_MESSAGE = "Server shut down while build in progress";

    public void execute()
    {
        transactionContext.executeInsideTransaction(new NullaryFunction<Object>()
        {
            public Object process()
            {
                List<Project> projects = projectManager.getProjects(true);
                for (Project project : projects)
                {
                    projectManager.abortUnfinishedBuilds(project, ABORT_MESSAGE);
                }

                List<User> users = userManager.getAllUsers();
                for(User user: users)
                {
                    buildManager.abortUnfinishedBuilds(user, ABORT_MESSAGE);
                }
                return null;
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

    public void setTransactionContext(TransactionContext transactionContext)
    {
        this.transactionContext = transactionContext;
    }
}
