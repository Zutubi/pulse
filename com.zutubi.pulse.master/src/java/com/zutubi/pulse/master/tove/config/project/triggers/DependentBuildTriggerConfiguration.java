package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.scheduling.NoopTrigger;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.tove.annotations.Form;
import com.zutubi.tove.annotations.SymbolicName;

/**
 * The trigger configuration for dependent build triggers.
 */
@Form(fieldOrder = { "name", "propagateStatus", "propagateVersion" })
@SymbolicName("zutubi.dependentBuildTriggerConfig")
public class DependentBuildTriggerConfiguration extends TriggerConfiguration
{
    /**
     * If true, build requests raised by this trigger will inherit the status
     * of the completed build.
     */
    private boolean propagateStatus;

    /**
     * If true, build requests raised by this trigger will inherit the version
     * of the completed build.
     */
    private boolean propagateVersion;

    public Trigger newTrigger()
    {
        return new NoopTrigger(getName(), Trigger.DEFAULT_GROUP);
    }

    public boolean isPropagateStatus()
    {
        return propagateStatus;
    }

    public void setPropagateStatus(boolean propagateStatus)
    {
        this.propagateStatus = propagateStatus;
    }

    public boolean isPropagateVersion()
    {
        return propagateVersion;
    }

    public void setPropagateVersion(boolean propagateVersion)
    {
        this.propagateVersion = propagateVersion;
    }
}
