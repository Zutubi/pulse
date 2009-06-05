package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.List;
import java.util.Arrays;

/**
 * Add the version field to the proejct dependencies configuration.
 */
public class AddDependencyVersionFieldUpgradeTask  extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_VERSION = "version";

    private static final String DEFAULT_VERSION = "${build.number}";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath("projects/*/dependencies"));
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_VERSION, DEFAULT_VERSION));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}

