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

package com.zutubi.pulse.master.build.queue;

import com.google.common.base.Predicate;
import com.zutubi.pulse.master.events.build.BuildRequestEvent;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;

/**
 * Predicate that is satisfied if the project state accepts a
 * build being triggered by the request.
 *
 * @param <T>
 *
 * @see com.zutubi.pulse.master.model.Project.State#acceptTrigger(boolean)
 */
public class CanBuildPredicate<T extends RequestHolder> implements Predicate<T>
{
    private ProjectManager projectManager;

    public boolean apply(RequestHolder holder)
    {
        BuildRequestEvent request = holder.getRequest();
        Project project = projectManager.getProject(request.getProjectConfig().getProjectId(), false);
        return project != null && project.getState().acceptTrigger(request.isPersonal());
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
