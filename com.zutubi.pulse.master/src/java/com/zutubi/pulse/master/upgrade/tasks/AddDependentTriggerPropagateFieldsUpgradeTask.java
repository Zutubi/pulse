package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;
import static com.zutubi.pulse.master.upgrade.tasks.RecordUpgraders.newAddProperty;

import java.util.List;
import java.util.Arrays;

/**
 * Upgrade task that adds the propagate status and version fields to the dependent build trigger configuration.
 */
public class AddDependentTriggerPropagateFieldsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String TYPE_DEPENDENCY_TRIGGER = "zutubi.dependentBuildTriggerConfig";

    private static final String PROPERTY_PROPAGATE_STATUS = "propagateStatus";

    private static final String DEFAULT_PROPAGATE_STATUS = "false";

    private static final String PROPERTY_PROPAGATE_VERSION = "propagateVersion";

    private static final String DEFAULT_PROPAGATE_VERSION = "false";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter (
                RecordLocators.newPathPattern(PathUtils.getPath("projects", PathUtils.WILDCARD_ANY_ELEMENT, "triggers")),
                TYPE_DEPENDENCY_TRIGGER
        );
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(newAddProperty(PROPERTY_PROPAGATE_STATUS, DEFAULT_PROPAGATE_STATUS),
                newAddProperty(PROPERTY_PROPAGATE_VERSION, DEFAULT_PROPAGATE_VERSION));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}