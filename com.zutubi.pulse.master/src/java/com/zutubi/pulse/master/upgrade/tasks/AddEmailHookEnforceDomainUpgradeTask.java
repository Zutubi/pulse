package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Collections;
import java.util.List;

import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;

/**
 * Adds the enforceDomain field to SendEmailTaskConfiguration.
 */
public class AddEmailHookEnforceDomainUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        // Find all hook tasks, filter down to email committers.
        RecordLocator triggerLocator = RecordLocators.newPathPattern(PathUtils.getPath("projects", WILDCARD_ANY_ELEMENT, "buildHooks", WILDCARD_ANY_ELEMENT, "task"));
        return RecordLocators.newTypeFilter(triggerLocator, "zutubi.sendEmailTaskConfig");
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Collections.singletonList(RecordUpgraders.newAddProperty("enforceDomain", "false"));
    }
}
