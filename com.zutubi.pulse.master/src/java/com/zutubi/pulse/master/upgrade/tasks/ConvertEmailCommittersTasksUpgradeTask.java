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
 * Converts existing email committers hook tasks to new send emails tasks.
 */
public class ConvertEmailCommittersTasksUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";

    private static final String TYPE_OLD = "zutubi.emailCommittersTaskConfig";
    private static final String TYPE_NEW = "zutubi.sendEmailTaskConfig";

    private static final String PROPERTY_HOOKS = "buildHooks";
    private static final String PROPERTY_TASK = "task";
    private static final String PROPERTY_CONTACTS = "emailContacts";
    private static final String PROPERTY_COMMITTERS = "emailCommitters";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        RecordLocator allHookTasks = RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_HOOKS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_TASK));
        return RecordLocators.newTypeFilter(allHookTasks, TYPE_OLD);
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(
                RecordUpgraders.newChangeSymbolicName(TYPE_OLD, TYPE_NEW),
                RecordUpgraders.newAddProperty(PROPERTY_CONTACTS, "false"),
                RecordUpgraders.newAddProperty(PROPERTY_COMMITTERS, "true")
        );
    }
}