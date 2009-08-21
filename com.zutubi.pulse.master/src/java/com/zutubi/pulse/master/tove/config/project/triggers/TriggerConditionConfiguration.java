package com.zutubi.pulse.master.tove.config.project.triggers;

import com.zutubi.pulse.master.trigger.TriggerCondition;
import com.zutubi.tove.annotations.SymbolicName;
import com.zutubi.tove.annotations.Table;
import com.zutubi.tove.config.api.AbstractConfiguration;

/**
 * Configuration for conditions which are applied before firing a trigger. 
 */
@SymbolicName("zutubi.triggerConditionConfig")
@Table(columns = "type")
public abstract class TriggerConditionConfiguration extends AbstractConfiguration
{
    public abstract Class<? extends TriggerCondition> conditionType();
}
