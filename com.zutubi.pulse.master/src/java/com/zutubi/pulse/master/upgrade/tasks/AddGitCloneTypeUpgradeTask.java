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

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;

import java.util.List;

import static java.util.Arrays.asList;

/**
 * Adds a new cloneType field to git configuration based on the current
 * trackSelectedBranch value.
 */
public class AddGitCloneTypeUpgradeTask extends AbstractRecordPropertiesUpgradeTask
{
    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_SCM = "scm";
    private static final String PROPERTY_CLONE_TYPE = "cloneType";
    private static final String PROPERTY_TRACK_SELECTED_BRANCH = "trackSelectedBranch";
    private static final String TYPE_GIT = "zutubi.gitConfig";
    private static final String CLONE_TYPE_NORMAL = "NORMAL";
    private static final String CLONE_TYPE_SELECTED_BRANCH = "SELECTED_BRANCH_ONLY";

    public boolean haltOnFailure()
    {
        return true;
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
        RecordUpgrader upgrader = new RecordUpgrader()
        {
            public void upgrade(String path, MutableRecord record)
            {
                if (record.containsKey(PROPERTY_CLONE_TYPE))
                {
                    return;
                }

                String trackSelected = (String) record.get(PROPERTY_TRACK_SELECTED_BRANCH);
                String value = CLONE_TYPE_NORMAL;
                if (trackSelected != null && Boolean.parseBoolean(trackSelected))
                {
                    value = CLONE_TYPE_SELECTED_BRANCH;
                }

                record.put(PROPERTY_CLONE_TYPE, value);
            }
        };

        return asList(upgrader, new DeletePropertyRecordUpgrader(PROPERTY_TRACK_SELECTED_BRANCH));
    }
}
