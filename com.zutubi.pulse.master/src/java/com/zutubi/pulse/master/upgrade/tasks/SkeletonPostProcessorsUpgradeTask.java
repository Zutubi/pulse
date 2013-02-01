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