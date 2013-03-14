package com.zutubi.pulse.master.upgrade.tasks;

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Upgrade task to add the description property to instances of the
 * ResourcePropertyConfiguration.
 */
public class AddResourcePropertyDescriptionUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_DESCRIPTION = "description";

    public boolean haltOnFailure()
    {
        return false;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(
                PathUtils.getPath("agents/*/resources/*/properties/*"),
                PathUtils.getPath("agents/*/resources/*/versions/*/properties/*"),
                PathUtils.getPath("projects/*/properties/*")
        );
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_DESCRIPTION, ""));
    }
}
