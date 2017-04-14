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
 * Adds skeletons for the globally-defined post-processors.  This is a
 * separate task to {@link com.zutubi.pulse.master.upgrade.tasks.GlobalPostProcessorsUpgradeTask}
 * as {@link com.zutubi.pulse.master.upgrade.tasks.MultiRecipeProjectTypeUpgradeTask}
 * must be run in between.
 */
public class SkeletonPostProcessorsUpgradeTask extends AbstractPredefinedRecordsUpgradeTask
{
    private static final String ARCHIVE_NAME = "skeletons";

    private static final String SCOPE_PROJECTS = "projects";
    private static final String PROPERTY_PROCESSORS = "postProcessors";

    public boolean haltOnFailure()
    {
        return true;
    }

    protected void execute(File tempDir) throws TaskException
    {
        final Record postProcessorSkeletons = loadRecords(tempDir, ARCHIVE_NAME);

        TemplatedScopeDetails details = new TemplatedScopeDetails(SCOPE_PROJECTS, recordManager);
        details.getHierarchy().forEach(new Function<ScopeHierarchy.Node, Boolean>()
        {
            public Boolean apply(ScopeHierarchy.Node node)
            {
                if (node.getParent() != null)
                {
                    recordManager.insert(SCOPE_PROJECTS + "/" + node.getId() + "/" + PROPERTY_PROCESSORS, postProcessorSkeletons);
                }

                return true;
            }
        });
    }

}