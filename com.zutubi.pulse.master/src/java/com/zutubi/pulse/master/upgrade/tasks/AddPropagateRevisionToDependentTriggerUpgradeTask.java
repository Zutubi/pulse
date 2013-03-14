package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * Add the propagate revision boolean property to dependency build triggers.
 */
public class AddPropagateRevisionToDependentTriggerUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String TYPE = "zutubi.dependentBuildTriggerConfig";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(RecordLocators.newPathPattern("projects/*/triggers/*"),TYPE);
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty("propagateRevision", "false"));
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
