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

import static com.zutubi.pulse.master.upgrade.tasks.RecordLocators.newPathPattern;
import static com.zutubi.pulse.master.upgrade.tasks.RecordLocators.newTypeFilter;

/**
 * Add the terminated result state to any build hook configurations that
 * currently contain the error result state.
 */
public class AddTerminatedResultStateToConfigurationsUpgradeTask extends BaseAddTerminatedResultStateUpgradeTask
{
    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newUnion(
                newPathPattern("projects/*/cleanup/*"),
                newTypeFilter(
                        newPathPattern("projects/*/triggers/*/conditions/*"),
                        "zutubi.projectStateTriggerConditionConfig"
                ),
                newTypeFilter(
                        newPathPattern("projects/*/triggers/*"),
                        "zutubi.buildCompletedConfig"
                )
        );
    }

    protected String getPropertyName()
    {
        return "states";
    }
}
