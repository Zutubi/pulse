package com.zutubi.pulse.master.trigger;

import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConditionConfiguration;

/**
 * Support base class for implementing {@link TriggerCondition}.
 */
public abstract class TriggerConditionSupport implements TriggerCondition
{
    private TriggerConditionConfiguration config;

    /**
     * Builds a condition, storing the configuration for later access.
     *
     * @param config configuration for this condition
     */
    protected TriggerConditionSupport(TriggerConditionConfiguration config)
    {
        this.config = config;
    }

    /**
     * Retrieves the configuration for this condition.
     *
     * @return this condition's configuration
     */
    public TriggerConditionConfiguration getConfig()
    {
        return config;
    }
}
