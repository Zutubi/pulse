package com.zutubi.pulse.master.tove.config.project;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.zutubi.pulse.master.tove.config.project.triggers.FireableTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.ManualTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerUtils;
import com.zutubi.tove.ui.model.ActionModel;

import java.util.List;

/**
 * Custom action handler for project triggers.
 */
public class ProjectConfigurationTriggerHandler extends AbstractTriggerHandler
{
    @Override
    protected ProjectConfiguration getProjectConfig(String path)
    {
        ProjectConfiguration project = configurationProvider.get(path, ProjectConfiguration.class);
        if (project == null)
        {
            throw new IllegalArgumentException("Path '" + path + "' does not exist");
        }
        return project;
    }

    @Override
    protected ManualTriggerConfiguration getTriggerConfig(String path, final String variant, ProjectConfiguration project)
    {
        List<ManualTriggerConfiguration> triggers = TriggerUtils.getTriggers(project, ManualTriggerConfiguration.class);
        Optional<ManualTriggerConfiguration> oTrigger = Iterables.tryFind(triggers, new Predicate<ManualTriggerConfiguration>()
        {
            @Override
            public boolean apply(ManualTriggerConfiguration input)
            {
                return input.getName().equals(variant);
            }
        });

        if (!oTrigger.isPresent())
        {
            throw new IllegalArgumentException("Project '" + project.getName() + "' has no manual trigger named '" + variant + "'");
        }

        return oTrigger.get();
    }

    @Override
    public ActionModel buildModel(FireableTriggerConfiguration trigger)
    {
        return new ActionModel("trigger", trigger.getName(), trigger.getName(), trigger.prompt());
    }
}
