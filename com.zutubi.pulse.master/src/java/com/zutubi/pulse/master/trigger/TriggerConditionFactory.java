package com.zutubi.pulse.master.trigger;

import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConditionConfiguration;

/**
 * Factory for building trigger conditions from their configuration.
 */
public interface TriggerConditionFactory
{
    /**
     * Create a condition using the given configuration.  The configuration is
     * passed to a single-argument constructor of the condition class.
     *
     * @param config the configuration to build the condition for
     * @return the new condition
     */
    TriggerCondition create(TriggerConditionConfiguration config);
}
