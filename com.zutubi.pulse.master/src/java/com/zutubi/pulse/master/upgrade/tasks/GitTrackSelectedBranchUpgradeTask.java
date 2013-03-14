package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * A task to add a new field to GitConfiguration to allow tracking of the
 * specified branch only.  See CIB-1952.
 */
public class GitTrackSelectedBranchUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        // Find all scms, filter down to git.
        RecordLocator locator = RecordLocators.newPathPattern(PathUtils.getPath("projects", PathUtils.WILDCARD_ANY_ELEMENT, "scm"));
        return RecordLocators.newTypeFilter(locator, "zutubi.gitConfig");
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty("trackSelectedBranch", "false"));
    }
}