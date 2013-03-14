package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * CIB-2768: remove crufty details property from change viewers.
 */
public class RemoveChangeViewerDetailsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern("projects/*/changeViewer");
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newDeleteProperty("details"));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
