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

import com.google.common.base.Function;
import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.Record;

import java.io.File;

/**
 * Adds the globally-defined report groups to the global project template, and
 * skeletons to all other projects.
 */
public class GlobalReportsUpgradeTask extends AbstractPredefinedRecordsUpgradeTask
{
    private static final String GLOBAL_ARCHIVE_NAME = "reports";
    private static final String SKELETONS_ARCHIVE_NAME = "skeletons";

    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_REPORT_GROUPS = "reportGroups";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected void execute(File tempDir) throws TaskException
    {
        addGlobal(createExpandDir(tempDir, GLOBAL_ARCHIVE_NAME));
        addSkeletons(createExpandDir(tempDir, SKELETONS_ARCHIVE_NAME));
    }

    private void addGlobal(File tempDir) throws TaskException
    {
        TemplatedScopeDetails details = new TemplatedScopeDetails(SCOPE_PROJECTS, recordManager);
        String globalProjectName = details.getHierarchy().getRoot().getId();

        Record reportGroups = loadRecords(tempDir, GLOBAL_ARCHIVE_NAME);
        recordManager.insert(SCOPE_PROJECTS + "/" + globalProjectName + "/" + PROPERTY_REPORT_GROUPS, reportGroups);
    }

    private void addSkeletons(File tempDir) throws TaskException
    {
        final Record reportGroupSkeletons = loadRecords(tempDir, SKELETONS_ARCHIVE_NAME);

        TemplatedScopeDetails details = new TemplatedScopeDetails(SCOPE_PROJECTS, recordManager);
        details.getHierarchy().forEach(new Function<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean apply(ScopeHierarchy.Node node)
            {
                if (node.getParent() != null)
                {
                    recordManager.insert(SCOPE_PROJECTS + "/" + node.getId() + "/" + PROPERTY_REPORT_GROUPS, reportGroupSkeletons);
                }

                return true;
            }
        });
    }

    private File createExpandDir(File tempDir, String name) throws TaskException
    {
        File expandDir = new File(tempDir, name);
        if (!expandDir.mkdir())
        {
            throw new TaskException("Cannot create directory '" + expandDir.getAbsolutePath() + "'");
        }

        return expandDir;
    }
}
