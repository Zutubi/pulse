package com.zutubi.pulse.master.trigger;

import com.zutubi.pulse.core.ConfiguredInstanceFactory;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConditionConfiguration;

/**
 * Trigger condition factory implemented using the standard support class.
 */
public class DefaultTriggerConditionFactory extends ConfiguredInstanceFactory<TriggerCondition, TriggerConditionConfiguration> implements TriggerConditionFactory
{
    protected Class<? extends TriggerCondition> getType(TriggerConditionConfiguration configuration)
    {
        return configuration.conditionType();
    }
}
