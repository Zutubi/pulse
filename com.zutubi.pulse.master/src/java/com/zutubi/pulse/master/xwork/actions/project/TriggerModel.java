package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.tove.config.project.triggers.ManualTriggerConfiguration;

/**
 * Represents a manual trigger for a project.
 */
public class TriggerModel
{
    private String name;
    private long handle;
    private boolean prompt;

    public TriggerModel(ManualTriggerConfiguration trigger)
    {
        name = trigger.getName();
        handle = trigger.getHandle();
        prompt = trigger.isPrompt();
    }

    public String getName()
    {
        return name;
    }

    public long getHandle()
    {
        return handle;
    }

    public boolean isPrompt()
    {
        return prompt;
    }
}
