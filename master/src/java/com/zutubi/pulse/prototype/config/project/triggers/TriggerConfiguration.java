package com.zutubi.pulse.prototype.config.project.triggers;

import com.zutubi.config.annotations.ExternalState;
import com.zutubi.config.annotations.SymbolicName;
import com.zutubi.config.annotations.Table;
import com.zutubi.config.annotations.Transient;
import com.zutubi.pulse.core.config.AbstractNamedConfiguration;
import com.zutubi.pulse.prototype.config.project.ProjectConfiguration;
import com.zutubi.pulse.scheduling.Trigger;

/**
 * 
 */
@Table(columns = {"name", "type", "state"})
@SymbolicName("zutubi.triggerConfig")
public abstract class TriggerConfiguration extends AbstractNamedConfiguration
{
    @ExternalState
    private long triggerId;

    public long getTriggerId()
    {
        return triggerId;
    }

    public void setTriggerId(long triggerId)
    {
        this.triggerId = triggerId;
    }

    public abstract Trigger newTrigger();

    @Transient
    public abstract String getType();

    public void update(Trigger trigger)
    {
        trigger.setName(getName());
    }

    protected String getTriggerName()
    {
        return getName();
    }

    protected String getTriggerGroup(ProjectConfiguration project)
    {
        return "project:" + project.getProjectId();
    }
}
