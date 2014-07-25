package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.MutableRecord;

import java.util.Arrays;
import java.util.List;

/**
 * Upgrade task that expands the propagateRevision boolean to an revisionHandling enum in dependent
 * triggers.
 */
public class DependentBuildTriggerRevisionHandlingUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_PROPAGATE_REVISION = "propagateRevision";
    private static final String PROPERTY_REVISION_HANDLING = "revisionHandling";

    public boolean haltOnFailure()
    {
        return true;
    }

    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(RecordLocators.newPathPattern("projects/*/triggers/*"), "zutubi.dependentBuildTriggerConfig");
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(new RecordUpgrader()
        {
            public void upgrade(String path, MutableRecord record)
            {
                if (record.containsKey(PROPERTY_REVISION_HANDLING) || !record.containsKey(PROPERTY_PROPAGATE_REVISION))
                {
                    return;
                }

                Boolean propagate = Boolean.valueOf((String) record.get(PROPERTY_PROPAGATE_REVISION));
                record.put(PROPERTY_REVISION_HANDLING, propagate ? "PROPAGATE_FROM_UPSTREAM" : "FLOAT_INDEPENDENTLY");
            }
        }, RecordUpgraders.newDeleteProperty(PROPERTY_PROPAGATE_REVISION));
    }
}
