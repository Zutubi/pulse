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

import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.util.TransactionContext;
import com.zutubi.tove.config.cleanup.RecordCleanupTask;

import java.util.Arrays;
import java.util.List;

/**
 * Adds a custom cleanup task for project configuration that deletes the
 * project state and build results.
 */
public class ProjectConfigurationCleanupTasks
{
    private ProjectManager projectManager;
    private TransactionContext transactionContext;

    public List<RecordCleanupTask> getTasks(ProjectConfiguration instance)
    {
        return Arrays.<RecordCleanupTask>asList(new ProjectStateCleanupTask(instance, projectManager, transactionContext));
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setTransactionContext(TransactionContext transactionContext)
    {
        this.transactionContext = transactionContext;
    }
}
