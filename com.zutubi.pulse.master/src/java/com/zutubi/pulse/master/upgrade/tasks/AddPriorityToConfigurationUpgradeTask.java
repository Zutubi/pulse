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

import java.util.List;

import static com.zutubi.pulse.master.upgrade.tasks.RecordUpgraders.newAddProperty;
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

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return asList(newAddProperty("priority", ""));
    }

    public boolean haltOnFailure()
    {
        return true;
    }
}
