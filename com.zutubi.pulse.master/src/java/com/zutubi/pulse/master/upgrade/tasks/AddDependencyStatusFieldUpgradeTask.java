package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.List;
import java.util.Arrays;

/**
 * Upgrade task that adds the status field to the project/dependencies
 */
public class AddDependencyStatusFieldUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_STATUS = "status";

    private static final String DEFAULT_STATUS = "integration";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath("projects", PathUtils.WILDCARD_ANY_ELEMENT, "dependencies"));
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_STATUS, DEFAULT_STATUS));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
