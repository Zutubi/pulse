package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * A task to add new fields to GitConfiguration to allow configuraiton of an
 * inactivity timeout.  See CIB-1932.
 */
public class GitInactivityTimeoutUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        // Find all scms, filter down to git.
        RecordLocator triggerLocator = RecordLocators.newPathPattern(PathUtils.getPath("projects", PathUtils.WILDCARD_ANY_ELEMENT, "scm"));
        return RecordLocators.newTypeFilter(triggerLocator, "zutubi.gitConfig");
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty("inactivityTimeoutEnabled", "false"),
                             RecordUpgraders.newAddProperty("inactivityTimeoutSeconds", "300"));
    }
}