package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * Removes the persistentWorkDir build option as it has moved to the new
 * bootstrap options.
 */
public class RemovePersistentWorkDirOptionUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern("projects/*/options");
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newDeleteProperty("persistentWorkDir"));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
