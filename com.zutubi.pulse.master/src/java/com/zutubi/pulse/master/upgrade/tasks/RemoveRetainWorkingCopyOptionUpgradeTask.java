package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * CIB-2365: remove the option to retain the working copies from the build options.
 */
public class RemoveRetainWorkingCopyOptionUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern("projects/*/options");
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newDeleteProperty("retainWorkingCopy"));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
