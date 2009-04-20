package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.List;
import java.util.Arrays;

/**
 * Upgrade task that adds the status field to the project/triggers
 */
public class AddDependencyStatusFieldUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String BUILD_COMPLETED_TRIGGER_SYMBOLIC_NAME = "zutubi.buildCompletedConfig";

    private static final String PROPERTY_PROPAGATE_STATUS = "propagateStatus";
    
    private static final String DEFAULT_PROPAGATE_STATUS = "true";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter (
                RecordLocators.newPathPattern(PathUtils.getPath("projects", PathUtils.WILDCARD_ANY_ELEMENT, "triggers")),
                BUILD_COMPLETED_TRIGGER_SYMBOLIC_NAME
        );
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_PROPAGATE_STATUS, DEFAULT_PROPAGATE_STATUS));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
