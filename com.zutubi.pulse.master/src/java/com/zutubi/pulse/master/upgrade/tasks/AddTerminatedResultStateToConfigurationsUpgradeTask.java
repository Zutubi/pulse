package com.zutubi.pulse.master.upgrade.tasks;

import static com.zutubi.pulse.master.upgrade.tasks.RecordLocators.newPathPattern;
import static com.zutubi.pulse.master.upgrade.tasks.RecordLocators.newTypeFilter;

/**
 * Add the terminated result state to any build hook configurations that
 * currently contain the error result state.
 */
public class AddTerminatedResultStateToConfigurationsUpgradeTask extends BaseAddTerminatedResultStateUpgradeTask
{
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newUnion(
                newPathPattern("projects/*/cleanup/*"),
                newTypeFilter(
                        newPathPattern("projects/*/triggers/*/conditions/*"),
                        "zutubi.projectStateTriggerConditionConfig"
                ),
                newTypeFilter(
                        newPathPattern("projects/*/triggers/*"),
                        "zutubi.buildCompletedConfig"
                )
        );
    }

    protected String getPropertyName()
    {
        return "states";
    }
}
