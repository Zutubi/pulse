package com.zutubi.pulse.restore;

import java.util.List;
import java.util.Arrays;
import java.io.File;

/**
 *
 *
 */
public abstract class AbstractArchivableComponent implements ArchiveableComponent
{
    public List<RestoreTask> getRestoreTasks(final File archiveComponentBase)
    {
        final ArchiveableComponent self = this;
        RestoreTask restoreDelegate = new RestoreTask()
        {
            private File archive = archiveComponentBase;
            private ArchiveableComponent delegate = self;

            public void execute() throws ArchiveException
            {
                delegate.restore(archive);
            }
        };
        
        return Arrays.asList(restoreDelegate);
    }
}
