package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

import static com.zutubi.pulse.master.upgrade.tasks.RecordUpgraders.newAddProperty;

/**
 * Upgrade task that adds the propagate status and version fields to the build completed trigger configuration.
 */
public class AddBuildCompletedTriggerPropagateFieldsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String BUILD_COMPLETED_TRIGGER_SYMBOLIC_NAME = "zutubi.buildCompletedConfig";

    private static final String PROPERTY_PROPAGATE_STATUS = "propagateStatus";

    private static final String DEFAULT_PROPAGATE_STATUS = "false";

    private static final String PROPERTY_PROPAGATE_VERSION = "propagateVersion";

    private static final String DEFAULT_PROPAGATE_VERSION = "false";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter (
                RecordLocators.newPathPattern(PathUtils.getPath("projects", PathUtils.WILDCARD_ANY_ELEMENT, "triggers")),
                BUILD_COMPLETED_TRIGGER_SYMBOLIC_NAME
        );
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(newAddProperty(PROPERTY_PROPAGATE_STATUS, DEFAULT_PROPAGATE_STATUS),
                newAddProperty(PROPERTY_PROPAGATE_VERSION, DEFAULT_PROPAGATE_VERSION));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}