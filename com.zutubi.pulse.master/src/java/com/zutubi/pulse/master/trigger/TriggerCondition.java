package com.zutubi.pulse.master.trigger;

import com.zutubi.pulse.master.model.Project;
import com.zutubi.pulse.master.tove.config.project.triggers.TriggerConditionConfiguration;

/**
 * Trigger conditions are applied before deciding to fire off a build from a
 * trigger.  They may be used to filter out unwanted firings.
 */
public interface TriggerCondition
{
    /**
     * Retrieves the configuration for this condition.
     *
     * @return the configuraiton for this condition
     */
    TriggerConditionConfiguration getConfig();

    /**
     * Applies this condition, indicating if it is satisfied and will allow a
     * build to be triggered.
     *
     * @param project the project that the trigger belongs to
     * @return true if this condition is satisfied
     */
    boolean satisfied(Project project);
}
