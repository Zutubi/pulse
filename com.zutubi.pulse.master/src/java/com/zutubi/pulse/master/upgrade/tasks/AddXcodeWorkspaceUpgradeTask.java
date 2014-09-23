package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * Adds new fields for workspaces and other "modern" things to the xcode command.
 */
public class AddXcodeWorkspaceUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern("projects/*/type/recipes/*/commands/*"),
                "zutubi.xcodeCommandConfig"
        );
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(
                RecordUpgraders.newAddProperty("workspace", ""),
                RecordUpgraders.newAddProperty("scheme", ""),
                RecordUpgraders.newAddProperty("destinations", new String[0]),
                RecordUpgraders.newAddProperty("arch", ""),
                RecordUpgraders.newAddProperty("sdk", "")
        );
    }
}
