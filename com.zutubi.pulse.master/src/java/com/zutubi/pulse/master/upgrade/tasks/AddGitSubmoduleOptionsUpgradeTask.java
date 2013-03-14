package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * Adds new git configuration options for submodules.
 */
public class AddGitSubmoduleOptionsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(RecordLocators.newPathPattern("projects/*/scm"), "zutubi.gitConfig");
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(
                RecordUpgraders.newAddProperty("submoduleProcessing", "NONE"),
                RecordUpgraders.newAddProperty("selectedSubmodules", "")
        );
    }
}
