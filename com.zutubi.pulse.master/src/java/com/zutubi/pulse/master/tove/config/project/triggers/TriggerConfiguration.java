package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.tove.annotations.ExternalState;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.AbstractNamedConfiguration;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;

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
