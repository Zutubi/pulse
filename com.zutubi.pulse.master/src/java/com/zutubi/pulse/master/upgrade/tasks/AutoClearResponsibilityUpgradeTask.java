package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * Upgrade task to add a new build options to automatically clear
 * responsibility on a successful build.
 */
public class AutoClearResponsibilityUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY = "autoClearResponsibility";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern("projects/*/options");
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY, "true"));
    }
}
