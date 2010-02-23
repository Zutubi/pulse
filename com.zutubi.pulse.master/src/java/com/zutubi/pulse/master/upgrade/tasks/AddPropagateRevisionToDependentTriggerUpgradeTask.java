package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.List;
import java.util.Arrays;

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

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty("propagateRevision", "false"));
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
