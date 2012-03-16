package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * Upgrades selected subscription conditions for changes for the new warning status.
 */
public class WarningStatusSelectedConditionsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern("users/*/preferences/subscriptions/*/condition"),
                "zutubi.selectedBuildsConditionConfig"
        );
    }

    @Override
    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(
                RecordUpgraders.newRenameProperty("unsuccessful", "broken"),
                RecordUpgraders.newAddProperty("failed", "false"),
                RecordUpgraders.newAddProperty("warnings", "false")
        );
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
