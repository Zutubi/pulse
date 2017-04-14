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

package com.zutubi.pulse.master.scheduling;

import com.zutubi.events.Event;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.scm.ScmChangeEvent;
import com.zutubi.pulse.master.scheduling.tasks.BuildProjectTask;

/**
 * A filter to ensure a trigger only fires for the SCM of the matching
 * project on an SCMChangeEvent.
 */
public class ScmChangeEventFilter implements EventTriggerFilter
{
    private ProjectManager projectManager;

    public boolean accept(Trigger trigger, Event event, TaskExecutionContext context)
    {
        ScmChangeEvent changeEvent = (ScmChangeEvent) event;
        ProjectConfiguration project = projectManager.getProjectConfig(trigger.getProject(), false);
        boolean accept = project != null && project.getName().equals(changeEvent.getProjectConfiguration().getName());

        context.put(BuildProjectTask.PARAM_JUMP_QUEUE_ALLOWED, false);
        context.put(BuildProjectTask.PARAM_SOURCE, "scm change");

        return accept;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
