package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds a new preference for customising columns on the server history tab.
 */
public class AddServerHistoryColumnsPreferenceUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_USERS = "users";
    private static final String PROPERTY_PREFERENCES = "preferences";
    private static final String PROPERTY_COLUMNS = "serverHistoryColumns";
    private static final String DEFAULT_VALUES = "project,number,revision,status,reason,tests,when,elapsed";

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_USERS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_PREFERENCES));
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_COLUMNS, DEFAULT_VALUES));
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
