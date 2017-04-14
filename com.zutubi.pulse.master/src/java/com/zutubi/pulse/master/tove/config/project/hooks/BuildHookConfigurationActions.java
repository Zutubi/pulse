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

package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.BuildSelectorConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.annotations.Permission;
import com.zutubi.tove.config.ConfigurationProvider;

import java.util.LinkedList;
import java.util.List;

/**
 * Actions for build hooks.
 */
public class BuildHookConfigurationActions
{
    public static final String ACTION_TRIGGER = "trigger";

    private BuildHookManager buildHookManager;
    private ProjectManager projectManager;
    private BuildManager buildManager;
    protected ConfigurationProvider configurationProvider;

    public List<String> getActions(BuildHookConfiguration instance)
    {
        List<String> actions = new LinkedList<String>();
        if (instance.canManuallyTriggerFor(null))
        {
            actions.add(ACTION_TRIGGER);
        }
        return actions;
    }

    @Permission(ProjectConfigurationActions.ACTION_TRIGGER_HOOK)
    public void doTrigger(BuildHookConfiguration instance, BuildSelectorConfiguration build)
    {
        ProjectConfiguration projectConfig = configurationProvider.getAncestorOfType(instance, ProjectConfiguration.class);
        if(projectConfig != null)
        {
            Project project = projectManager.getProject(projectConfig.getProjectId(), true);
            if(project != null)
            {
                BuildResult buildResult = buildManager.getByProjectAndVirtualId(project, build.getBuild());
                if(buildResult != null)
                {
                    buildHookManager.manualTrigger(instance, buildResult);
                }
            }
        }
    }

    public void setBuildHookManager(BuildHookManager buildHookManager)
    {
        this.buildHookManager = buildHookManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
