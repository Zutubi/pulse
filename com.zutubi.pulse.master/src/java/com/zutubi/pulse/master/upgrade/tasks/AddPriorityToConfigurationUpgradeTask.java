package com.zutubi.pulse.master.upgrade.tasks;

import static com.zutubi.pulse.master.upgrade.tasks.RecordUpgraders.newAddProperty;

import java.util.List;
import static java.util.Arrays.asList;

/**
 * Add the priority property to the build options and build stage
 * configurations.
 */
public class AddPriorityToConfigurationUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newUnion(
            RecordLocators.newPathPattern("projects/*/options"),
            RecordLocators.newPathPattern("projects/*/stages/*")
        );
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return asList(newAddProperty("priority", ""));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
