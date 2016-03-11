package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.tove.config.project.AbstractTriggerHandler;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.ui.model.ActionModel;

/**
 * Handler for firing triggers directly from their config pages.
 */
public class FireableTriggerConfigurationFireHandler extends AbstractTriggerHandler
{
    @Override
    protected ProjectConfiguration getProjectConfig(String path)
    {
        ProjectConfiguration project = configurationProvider.getAncestorOfType(path, ProjectConfiguration.class);
        if (project == null)
        {
            throw new IllegalArgumentException("Path '" + path + "' does not have an ancestor project");
        }
        return project;
    }

    @Override
    protected FireableTriggerConfiguration getTriggerConfig(String path, String variant, ProjectConfiguration project)
    {
        FireableTriggerConfiguration triggerConfig = configurationProvider.get(path, FireableTriggerConfiguration.class);
        if (triggerConfig == null)
        {
            throw new IllegalArgumentException("Path '" + path + "' does not exist or does not reference a fireable trigger");
        }

        return triggerConfig;
    }

    @Override
    protected ActionModel buildModel(FireableTriggerConfiguration trigger)
    {
        return new ActionModel(FireableTriggerConfigurationActions.ACTION_FIRE, "fire now", null, trigger.prompt());
    }
}
