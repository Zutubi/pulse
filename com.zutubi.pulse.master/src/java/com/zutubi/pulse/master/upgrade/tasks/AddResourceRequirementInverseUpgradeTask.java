package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Upgrade task to add the inverse property to ResourceRequirementConfiguration.
 */
public class AddResourceRequirementInverseUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_INVERSE = "inverse";

    public boolean haltOnFailure()
    {
        return false;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(
                PathUtils.getPath("projects/*/requirements/*"),
                PathUtils.getPath("projects/*/stages/*/requirements/*")
        );
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_INVERSE, "false"));
    }
}
