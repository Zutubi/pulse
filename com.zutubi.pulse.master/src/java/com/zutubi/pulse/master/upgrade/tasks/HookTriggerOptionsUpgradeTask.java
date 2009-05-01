package com.zutubi.pulse.master.upgrade.tasks;

import java.util.Arrays;
import java.util.List;

/**
 * Upgrade task to add new triggering options to auto hook configurations.
 */
public class HookTriggerOptionsUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_RUN_FOR_PERSONAL = "runForPersonal";
    private static final String PROPERTY_ALLOW_MANUAL_TRIGGER = "allowManualTrigger";

    public boolean haltOnFailure()
    {
        return false;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(RecordLocators.newPathPattern("projects/*/buildHooks/*"),
                "zutubi.postBuildHookConfig",
                "zutubi.preBuildHookConfig",
                "zutubi.postStageHookConfig"
        );
    }

    protected List<RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(
                RecordUpgraders.newAddProperty(PROPERTY_RUN_FOR_PERSONAL, "false"),
                RecordUpgraders.newAddProperty(PROPERTY_ALLOW_MANUAL_TRIGGER, "true")
        );
    }
}