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

import java.util.Arrays;
import java.util.List;

/**
 * Adds new fields for workspaces and other "modern" things to the xcode command.
 */
public class AddXcodeWorkspaceUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern("projects/*/type/recipes/*/commands/*"),
                "zutubi.xcodeCommandConfig"
        );
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(
                RecordUpgraders.newAddProperty("workspace", ""),
                RecordUpgraders.newAddProperty("scheme", ""),
                RecordUpgraders.newAddProperty("destinations", new String[0]),
                RecordUpgraders.newAddProperty("arch", ""),
                RecordUpgraders.newAddProperty("sdk", "")
        );
    }
}
