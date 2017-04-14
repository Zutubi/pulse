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

package com.zutubi.pulse.master.bootstrap.tasks;

import com.zutubi.pulse.master.model.*;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.pulse.servercore.bootstrap.StartupTask;
import com.zutubi.util.NullaryFunction;

import java.util.List;

/**
 * Aborts incomplete builds found on startup.
 */
public class BuildAborterStartupTask implements StartupTask
{
    public static final String ABORT_MESSAGE = "Server shut down while build in progress";

    private TransactionContext transactionContext;
    private ProjectManager projectManager;
    private BuildManager buildManager;
    private UserManager userManager;

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
                for (User user: users)
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
