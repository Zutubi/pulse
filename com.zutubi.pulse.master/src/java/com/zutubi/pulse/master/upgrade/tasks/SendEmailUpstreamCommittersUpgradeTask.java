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

import static com.zutubi.tove.type.record.PathUtils.WILDCARD_ANY_ELEMENT;

/**
 * Adds a new includeUpstreamCommitters option to the send email hook tasks.
 */
public class SendEmailUpstreamCommittersUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_HOOKS = "buildHooks";
    private static final String PROPERTY_TASK = "task";

    private static final String PROPERTY_INCLUDE_UPSTREAM = "includeUpstreamCommitters";

    private static final String TYPE_SEND_EMAIL = "zutubi.sendEmailTaskConfig";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected RecordLocator getRecordLocator()
    {
        RecordLocator triggerLocator = RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, WILDCARD_ANY_ELEMENT, PROPERTY_HOOKS, WILDCARD_ANY_ELEMENT, PROPERTY_TASK));
        return RecordLocators.newTypeFilter(triggerLocator, TYPE_SEND_EMAIL);
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return Arrays.asList(RecordUpgraders.newAddProperty(PROPERTY_INCLUDE_UPSTREAM, "false"));
    }
}