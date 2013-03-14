package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * Add the settingsFile property to maven2 commands.
 */
public class AddSettingsFileArgumentToMaven2ConfigurationUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_NAME = "settingsFile";
    private static final String PROPERTY_DEFAULT = "";
    private static final String MAVEN2_SYMBOLIC_NAME = "zutubi.maven2CommandConfig";

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern("projects/*/type/recipes/*/commands/*"),
                MAVEN2_SYMBOLIC_NAME
        );
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_NAME, PROPERTY_DEFAULT));
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
