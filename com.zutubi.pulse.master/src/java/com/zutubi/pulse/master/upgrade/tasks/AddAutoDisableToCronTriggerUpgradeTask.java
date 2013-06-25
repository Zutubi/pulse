package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * Adds the disableAfterFiring field to cron triggers (for CIB-2959).
 */
public class AddAutoDisableToCronTriggerUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(RecordLocators.newPathPattern("projects/*/triggers/*"), "zutubi.cronTriggerConfig");
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty("disableAfterFiring", "false"));
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
