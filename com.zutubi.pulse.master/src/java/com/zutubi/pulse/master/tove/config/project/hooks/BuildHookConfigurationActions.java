package com.zutubi.pulse.master.tove.config.project.hooks;

import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.tove.config.project.BuildSelectorConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
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
