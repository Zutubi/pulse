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

import com.zutubi.tove.type.record.PathUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Upgrade task to add two properties to the users dashboard configuration.  The sortProjectsAlphabetically
 * and the sortGroupsAlphabetically.  Both default to true.
 */
public class AddDashboardSortPropertiesUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        RecordLocator locator = RecordLocators.newPathPattern(PathUtils.getPath("users", PathUtils.WILDCARD_ANY_ELEMENT, "preferences/dashboard"));
        return RecordLocators.newTypeFilter(locator, "zutubi.dashboardConfig");
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty("sortProjectsAlphabetically", "true"),
                             RecordUpgraders.newAddProperty("sortGroupsAlphabetically", "true"));
    }
}
