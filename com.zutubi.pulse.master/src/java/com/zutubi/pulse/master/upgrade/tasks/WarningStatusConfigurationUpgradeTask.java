/* Copyright 2017 Zutubi Pty Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
