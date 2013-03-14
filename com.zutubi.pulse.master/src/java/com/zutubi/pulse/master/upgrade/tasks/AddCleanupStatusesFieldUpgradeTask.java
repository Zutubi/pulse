package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * Add the statuses field to the cleanup configurations.
 */
public class AddCleanupStatusesFieldUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_STATUSES = "statuses";

    protected RecordLocator getRecordLocator()
    {
         return RecordLocators.newPathPattern("projects/*/cleanup/*");
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_STATUSES, new String[0]));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
