package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.core.config.ResourcePropertyConfiguration;
import com.zutubi.pulse.master.scheduling.Trigger;
import com.zutubi.pulse.master.tove.config.project.ProjectConfiguration;
import com.zutubi.tove.annotations.ExternalState;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.annotations.Transient;
import com.zutubi.tove.config.api.AbstractNamedConfiguration;
import com.zutubi.i18n.Messages;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 
 */
@Table(columns = {"name", "type", "state"})
@SymbolicName("zutubi.triggerConfig")
public abstract class TriggerConfiguration extends AbstractNamedConfiguration
{
    @ExternalState
    private long triggerId;
    private Map<String, ResourcePropertyConfiguration> properties = new LinkedHashMap<String, ResourcePropertyConfiguration>();

    public long getTriggerId()
    {
        return triggerId;
    }

    public void setTriggerId(long triggerId)
    {
        this.triggerId = triggerId;
    }

    public Map<String, ResourcePropertyConfiguration> getProperties()
    {
        return properties;
    }

    public void setProperties(Map<String, ResourcePropertyConfiguration> properties)
    {
        this.properties = properties;
    }

    public abstract Trigger newTrigger();

    @Transient
    public String getType()
    {
        return Messages.getInstance(this).format("type.label");
    }

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
