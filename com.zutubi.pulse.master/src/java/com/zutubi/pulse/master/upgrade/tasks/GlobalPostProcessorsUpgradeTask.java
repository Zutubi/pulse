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
