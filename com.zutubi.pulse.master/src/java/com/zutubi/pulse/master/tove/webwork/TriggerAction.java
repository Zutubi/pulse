package com.zutubi.pulse.master.tove.webwork;

import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.ManualTriggerConfiguration;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerUtils;
import com.zutubi.tove.config.api.Configuration;
import com.zutubi.util.StringUtils;

import java.util.List;

/**
 * Customised action for handling triggers (via the config UI) which redirects to the manual
 * trigger action if a specific trigger was chosen.
 */
public class TriggerAction extends GenericAction
{
    private long triggerHandle;

    public long getTriggerHandle()
    {
        return triggerHandle;
    }

    @Override
    protected String executeSingleAction(Configuration config)
    {
        String argument = getArgument();
        if (StringUtils.stringSet(argument))
        {
            ProjectConfiguration project = (ProjectConfiguration) config;
            List<ManualTriggerConfiguration> triggers = TriggerUtils.getTriggers(project, ManualTriggerConfiguration.class);
            for (ManualTriggerConfiguration trigger: triggers)
            {
                if (trigger.getName().equals(argument))
                {
                    triggerHandle = trigger.getHandle();
                    setCustomAction("manualTrigger");
                    return "chain";
                }
            }
        }

        return super.executeSingleAction(config);
    }
}
