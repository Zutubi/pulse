package com.zutubi.pulse.master.upgrade.tasks;

import static com.zutubi.pulse.master.upgrade.tasks.RecordUpgraders.newAddProperty;

import static java.util.Arrays.asList;
import java.util.List;

/**
 * Upgrade task to add the enabled (default true) flag to all project commands and stages.
 */
public class AddEnableDisableToProjectCommandsAndStagesUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newUnion(
            RecordLocators.newPathPattern("projects/*/type/recipes/*/commands/*"),
            RecordLocators.newPathPattern("projects/*/stages/*")        
        );
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return asList(
                newAddProperty("enabled", Boolean.toString(true))
        );
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
