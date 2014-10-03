package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.model.BuildReason;
import com.zutubi.pulse.master.model.NamedManualTriggerBuildReason;
import com.zutubi.pulse.master.model.ProjectManager;
import com.zutubi.pulse.master.model.TriggerOptions;
import com.zutubi.pulse.master.scheduling.SchedulingException;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.ProjectConfigurationActions;
import com.zutubi.tove.annotations.Permission;
import com.zutubi.tove.config.ConfigurationProvider;

import java.util.List;

/**
 */
public class FireableTriggerConfigurationActions extends TriggerConfigurationActions
{
    public static final String ACTION_FIRE = "fire";

    private ConfigurationProvider configurationProvider;
    private ProjectManager projectManager;

    public List<String> getActions(TriggerConfiguration config)
    {
        List<String> actions = super.getActions(config);
        actions.add(ACTION_FIRE);
        return actions;
    }

    @Permission(ProjectConfigurationActions.ACTION_TRIGGER)
    public void doFire(FireableTriggerConfiguration config) throws SchedulingException
    {
        ProjectConfiguration projectConfiguration = configurationProvider.getAncestorOfType(config, ProjectConfiguration.class);
        if (projectConfiguration != null)
        {
            BuildReason reason = new NamedManualTriggerBuildReason(config.getName(), SecurityUtils.getLoggedInUsername());
            TriggerOptions options = new TriggerOptions(reason, ProjectManager.TRIGGER_CATEGORY_MANUAL);
            options.setProperties(config.getProperties().values());
            projectManager.triggerBuild(projectConfiguration, options, null);
        }
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
