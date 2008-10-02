package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * A task to add new propagateRevision and supercedeQueued fields to build
 * completed triggers.  See CIB-1452.
 */
public class BuildCompletedTriggerPropagateRevisionUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public String getName()
    {
        return "Build Completed Trigger Propagate Revisions";
    }

    public String getDescription()
    {
        return "Adds new configuration fields to build completed triggers to support propagating the build revision";
    }

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        // Find all triggers, filter down to build completed triggers.
        RecordLocator triggerLocator = RecordLocators.newPathPattern(PathUtils.getPath("projects", PathUtils.WILDCARD_ANY_ELEMENT, "triggers", PathUtils.WILDCARD_ANY_ELEMENT));
        return RecordLocators.newTypeFilter(triggerLocator, "zutubi.buildCompletedConfig");
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty("propagateRevision", "false"),
                             RecordUpgraders.newAddProperty("supercedeQueued", "false"));
    }
}
