package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Adds a new properties to dashboard preferences to control display of
 * ungrouped projects.
 */
public class DashboardShowUngroupedUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    // Scope and path to the records we are interested in
    private static final String SCOPE_USERS = "users";
    private static final String PROPERTY_PREFERENCES = "preferences";
    private static final String PROPERTY_DASHBOARD = "dashboard";

    // New property
    private static final String PROPERTY_SHOW_UNGROUPED = "showUngrouped";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_USERS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_PREFERENCES, PROPERTY_DASHBOARD));
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(
                RecordUpgraders.newAddProperty(PROPERTY_SHOW_UNGROUPED, Boolean.toString(true))
        );
    }
}
