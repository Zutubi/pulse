package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.List;
import java.util.Arrays;

/**
 * Upgrade task to add two properties to the users dashboard configuration.  The sortProjectsAlphabetically
 * and the sortGroupsAlphabetically.  Both default to true.
 */
public class AddDashboardSortPropertiesUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        RecordLocator locator = RecordLocators.newPathPattern(PathUtils.getPath("users", PathUtils.WILDCARD_ANY_ELEMENT, "preferences/dashboard"));
        return RecordLocators.newTypeFilter(locator, "zutubi.dashboardConfig");
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty("sortProjectsAlphabetically", "true"),
                             RecordUpgraders.newAddProperty("sortGroupsAlphabetically", "true"));
    }
}
