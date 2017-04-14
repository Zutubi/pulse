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

import com.zutubi.pulse.master.util.monitor.TaskException;
import com.zutubi.tove.type.record.Record;

import java.io.File;

/**
 * Adds the globally-defined default post-processors to the global project
 * template (skeletons are added later by {@link SkeletonPostProcessorsUpgradeTask}
 * after the old postProcessors are removed by {@link com.zutubi.pulse.master.upgrade.tasks.MultiRecipeProjectTypeUpgradeTask}).
 */
public class GlobalPostProcessorsUpgradeTask extends AbstractPredefinedRecordsUpgradeTask
{
    private static final String ARCHIVE_NAME = "processors";

    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_PROCESSORS = "postProcessors";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected void execute(File tempDir) throws TaskException
    {
        TemplatedScopeDetails details = new TemplatedScopeDetails(SCOPE_PROJECTS, recordManager);
        String globalProjectName = details.getHierarchy().getRoot().getId();

        Record postProcessors = loadRecords(tempDir, ARCHIVE_NAME);
        recordManager.insert(SCOPE_PROJECTS + "/" + globalProjectName + "/" + PROPERTY_PROCESSORS, postProcessors);
    }
}
