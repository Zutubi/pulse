package com.zutubi.pulse.master.upgrade.tasks;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Adds new fields to subscriptions for choosing to attach log files to
 * notifications.
 */
public class AddAttachLogOptionsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newUnion(
                RecordLocators.newTypeFilter(RecordLocators.newPathPattern("projects/*/buildHooks/*/task"), "zutubi.sendEmailTaskConfig"),
                RecordLocators.newPathPattern("users/*/preferences/subscriptions/*"));
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return asList(
                RecordUpgraders.newAddProperty("attachLogs", "false"),
                RecordUpgraders.newAddProperty("logLineLimit", "50")
        );
    }
}
