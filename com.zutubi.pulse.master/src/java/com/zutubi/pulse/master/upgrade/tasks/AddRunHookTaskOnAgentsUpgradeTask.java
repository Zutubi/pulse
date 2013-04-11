package com.zutubi.pulse.master.upgrade.tasks;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Adds the runTaskOnAgents property to stage hooks.
 */
public class AddRunHookTaskOnAgentsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return false;
    }

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern("projects/*/buildHooks/*"),
                "zutubi.preStageHookConfig", "zutubi.postStageHookConfig", "zutubi.terminateStageHookConfig");
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return asList(RecordUpgraders.newAddProperty("runTaskOnAgents", "false"));
    }
}
