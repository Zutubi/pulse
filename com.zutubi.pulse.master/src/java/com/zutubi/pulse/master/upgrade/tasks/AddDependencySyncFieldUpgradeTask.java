package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Add the version field to the proejct dependencies configuration.
 */
public class AddDependencySyncFieldUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_SYNC = "syncDestination";
    private static final String DEFAULT_VALUE = "true";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath("projects/*/dependencies"));
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_SYNC, DEFAULT_VALUE));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
