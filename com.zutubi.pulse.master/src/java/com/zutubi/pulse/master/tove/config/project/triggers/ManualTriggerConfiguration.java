package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.scheduling.NoopTrigger;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * A manual trigger is presented as an action in the Pulse UI, so the user can trigger the project
 * directly.  It bundles up default configuration and optionally will present a prompt.
 */
@Form(fieldOrder = { "name", "prompt", "rebuildUpstreamDependencies" })
@SymbolicName("zutubi.manualTriggerConfig")
public class ManualTriggerConfiguration extends FireableTriggerConfiguration
{
    private boolean prompt = true;
    private boolean rebuildUpstreamDependencies = false;

    public boolean isPrompt()
    {
        return prompt;
    }

    public void setPrompt(boolean prompt)
    {
        this.prompt = prompt;
    }

    public boolean isRebuildUpstreamDependencies()
    {
        return rebuildUpstreamDependencies;
    }

    public void setRebuildUpstreamDependencies(boolean rebuildUpstreamDependencies)
    {
        this.rebuildUpstreamDependencies = rebuildUpstreamDependencies;
    }

    @Override
    public Trigger newTrigger()
    {
        return new NoopTrigger(getName(), Trigger.DEFAULT_GROUP);
    }

    @Override
    public boolean prompt()
    {
        return isPrompt();
    }

    @Override
    public boolean rebuildUpstream()
    {
        return rebuildUpstreamDependencies;
    }
}
