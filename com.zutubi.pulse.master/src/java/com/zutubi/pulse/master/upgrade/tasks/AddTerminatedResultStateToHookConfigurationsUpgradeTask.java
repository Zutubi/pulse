package com.zutubi.pulse.master.upgrade.tasks;

import static com.zutubi.pulse.master.upgrade.tasks.RecordLocators.newPathPattern;

/**
 * Add the terminated result state to any build hook configurations that
 * currently contain the error result state.
 */
public class AddTerminatedResultStateToHookConfigurationsUpgradeTask extends BaseAddTerminatedResultStateUpgradeTask
{
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                newPathPattern("projects/*/buildHooks/*"),
                "zutubi.postBuildHookConfig",
                "zutubi.postStageHookConfig"
        );
    }

    protected String getPropertyName()
    {
        return "runForStates";
    }
}