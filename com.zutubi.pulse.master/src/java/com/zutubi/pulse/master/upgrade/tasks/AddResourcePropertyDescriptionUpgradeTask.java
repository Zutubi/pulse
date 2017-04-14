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
 * Upgrade task to add the description property to instances of the
 * ResourcePropertyConfiguration.
 */
public class AddResourcePropertyDescriptionUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String PROPERTY_DESCRIPTION = "description";

    public boolean haltOnFailure()
    {
        return false;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newPathPattern(
                PathUtils.getPath("agents/*/resources/*/properties/*"),
                PathUtils.getPath("agents/*/resources/*/versions/*/properties/*"),
                PathUtils.getPath("projects/*/properties/*")
        );
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_DESCRIPTION, ""));
    }
}
