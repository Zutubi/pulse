package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds a new preference for the default display of timestamps.
 */
public class AddTimestampDisplayPreferenceUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_USERS = "users";
    private static final String PROPERTY_PREFERENCES = "preferences";
    private static final String PROPERTY_TIMESTAMP_DISPLAY = "defaultTimestampDisplay";
    private static final String DEFAULT_VALUE = "RELATIVE";

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_USERS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_PREFERENCES));
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_TIMESTAMP_DISPLAY, DEFAULT_VALUE));
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
