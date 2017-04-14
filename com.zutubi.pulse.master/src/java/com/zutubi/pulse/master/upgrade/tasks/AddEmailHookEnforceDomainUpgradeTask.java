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

import java.util.Collections;
import java.util.List;

import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;

/**
 * Adds the enforceDomain field to SendEmailTaskConfiguration.
 */
public class AddEmailHookEnforceDomainUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        // Find all hook tasks, filter down to email committers.
        RecordLocator triggerLocator = RecordLocators.newPathPattern(PathUtils.getPath("projects", WILDCARD_ANY_ELEMENT, "buildHooks", WILDCARD_ANY_ELEMENT, "task"));
        return RecordLocators.newTypeFilter(triggerLocator, "zutubi.sendEmailTaskConfig");
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Collections.singletonList(RecordUpgraders.newAddProperty("enforceDomain", "false"));
    }
}
