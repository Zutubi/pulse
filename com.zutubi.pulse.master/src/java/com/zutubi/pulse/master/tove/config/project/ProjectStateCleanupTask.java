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

package com.zutubi.pulse.master.tove.config.project;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.DatabaseStateCleanupTaskSupport;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.tove.config.ToveRuntimeException;

/**
 * Cleans up the state associated with a deleted project.
 */
class ProjectStateCleanupTask extends DatabaseStateCleanupTaskSupport
{
    private ProjectConfiguration instance;
    private ProjectManager projectManager;

    public ProjectStateCleanupTask(ProjectConfiguration instance, ProjectManager projectManager, TransactionContext transactionContext)
    {
        super(instance.getConfigurationPath(), transactionContext);
        this.projectManager = projectManager;
        this.instance = instance;
    }

    public void cleanupState()
    {
        final long projectId = instance.getProjectId();

        projectManager.runUnderProjectLocks(new Runnable()
        {
            public void run()
            {
                Project project = projectManager.getProject(projectId, true);
                if (project != null)
                {
                    if (project.isTransitionValid(Project.Transition.DELETE))
                    {
                        projectManager.delete(project);
                    }
                    else
                    {
                        Project.State state = project.getState();
                        if (state.isBuilding())
                        {
                            throw new ToveRuntimeException("Unable to delete project as a build is running.  The project may be deleted when it becomes idle (consider pausing the project).");
                        }
                        else
                        {
                            throw new ToveRuntimeException("Unable to delete project while in state '" + state + "'");
                        }
                    }
                }
            }
        }, projectId);
    }
}
