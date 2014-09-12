package com.zutubi.pulse.master.upgrade.tasks;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Adds the runTaskOnAgents property to post build hooks.
 */
public class AddRunBuildHookTaskOnAgentsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return false;
    }

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern("projects/*/buildHooks/*"), "zutubi.postBuildHookConfig");
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return asList(RecordUpgraders.newAddProperty("runTaskOnAgents", "false"));
    }
}
