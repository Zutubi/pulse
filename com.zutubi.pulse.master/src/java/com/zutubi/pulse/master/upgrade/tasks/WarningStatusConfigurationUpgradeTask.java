package com.zutubi.pulse.master.upgrade.tasks;

import com.google.common.base.Function;
import com.zutubi.util.CollectionUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Updates various configuration records that refer to result states to account
 * for the new warning state.
 */
public class WarningStatusConfigurationUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    @Override
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newUnion(
                RecordLocators.newPathPattern("projects/*/cleanup/*"),
                RecordLocators.newTypeFilter(
                        RecordLocators.newPathPattern("projects/*/buildHooks/*"),
                        "zutubi.postBuildHookConfig", "zutubi.postStageHookConfig"
                ),
                RecordLocators.newTypeFilter(
                        RecordLocators.newPathPattern("projects/*/triggers/*"),
                        "zutubi.buildCompletedConfig"
                ),
                RecordLocators.newTypeFilter(
                        RecordLocators.newPathPattern("projects/*/triggers/*/conditions/*"),
                        "zutubi.projectStateTriggerConditionConfig"
                )
        );
    }

    @Override
    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        Function<Object, Object> editFn = new Function<Object, Object>()
        {
            public Object apply(Object o)
            {
                if (o != null && o instanceof String[])
                {
                    String[] states = (String[]) o;
                    if (CollectionUtils.contains(states, "SUCCESS"))
                    {
                        String[] edited = new String[states.length + 1];
                        System.arraycopy(states, 0, edited, 0, states.length);
                        edited[states.length] = "WARNINGS";
                        o = edited;
                    }
                }

                return o;
            }
        };

        return Arrays.asList(
                RecordUpgraders.newEditProperty("states", editFn),
                RecordUpgraders.newEditProperty("runForStates", editFn)
        );
    }

    public boolean haltOnFailure()
    {
        return false;
    }
}
