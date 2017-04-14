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

package com.zutubi.pulse.master.trigger;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.ProjectStateTriggerConditionConfiguration;
import com.zutubi.util.logging.Logger;

/**
 * A trigger condition that checks the state of another project.
 */
public class ProjectStateTriggerCondition extends TriggerConditionSupport
{
    private static final Logger LOG = Logger.getLogger(ProjectStateTriggerCondition.class);

    private BuildManager buildManager;
    private ProjectManager projectManager;

    public ProjectStateTriggerCondition(ProjectStateTriggerConditionConfiguration config)
    {
        super(config);
    }

    public boolean satisfied(Project project)
    {
        ProjectStateTriggerConditionConfiguration config = (ProjectStateTriggerConditionConfiguration) getConfig();
        ProjectConfiguration otherProjectConfig = config.getProject();
        Project otherProject = projectManager.getProject(otherProjectConfig.getProjectId(), true);
        if (otherProject == null)
        {
            LOG.warning("Project state trigger condition configured on project '" + project.getName() + "' refers to unknown project '" + otherProjectConfig.getName() + "'");
            return false;
        }

        BuildResult buildResult = buildManager.getLatestCompletedBuildResult(otherProject);
        return buildResult != null && config.getStates().contains(buildResult.getState());
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
