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

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Adds a new clone depth field to git configuration.
 */
public class AddGitCloneDepthUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_SCM = "scm";
    private static final String PROPERTY_CLONE_DEPTH = "cloneDepth";
    private static final String TYPE_GIT = "zutubi.gitConfig";

    public boolean haltOnFailure()
    {
        return false;
    }

    protected RecordLocator getRecordLocator()
    {
        return RecordLocators.newTypeFilter(
                RecordLocators.newPathPattern(PathUtils.getPath(SCOPE_PROJECTS, PathUtils.WILDCARD_ANY_ELEMENT, PROPERTY_SCM)),
                TYPE_GIT
        );
    }

    protected List<? extends RecordUpgrader> getRecordUpgraders()
    {
        return asList(new AddPropertyRecordUpgrader(PROPERTY_CLONE_DEPTH, "32"));
    }
}

