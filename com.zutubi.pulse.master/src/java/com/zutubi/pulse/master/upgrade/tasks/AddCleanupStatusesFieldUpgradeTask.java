package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.List;
import java.util.Arrays;

/**
 * Add the statuses field to the cleanup configurations.
 */
public class AddCleanupStatusesFieldUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_STATUSES = "statuses";

    protected RecordLocator getRecordLocator()
    {
         return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern(PathUtils.getPath("projects", PathUtils.WILDCARD_ANY_ELEMENT, "cleanup", PathUtils.WILDCARD_ANY_ELEMENT)),
                "zutubi.cleanupConfig"
        );
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_STATUSES, new String[0]));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
