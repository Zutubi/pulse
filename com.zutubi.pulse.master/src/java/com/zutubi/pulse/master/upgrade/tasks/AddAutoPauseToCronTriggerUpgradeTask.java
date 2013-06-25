package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * Adds the pauseAfterFiring field to cron triggers (for CIB-2959).
 */
public class AddAutoPauseToCronTriggerUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(RecordLocators.newPathPattern("projects/*/triggers/*"), "zutubi.cronTriggerConfig");
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty("pauseAfterFiring", "false"));
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
