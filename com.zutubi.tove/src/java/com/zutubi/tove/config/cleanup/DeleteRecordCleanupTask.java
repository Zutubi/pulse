package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.type.record.RecordManager;

import java.util.Set;

/**
 * A reference cleanup task that deletes a record.  Any tasks needed to
 * cleanup references to the deleted record will cascade off this.
 */
public class DeleteRecordCleanupTask extends RecordCleanupTaskSupport
{
    private boolean internal;

    public DeleteRecordCleanupTask(String path, boolean internal)
    {
        super(path);
        this.internal = internal;
    }

    public void run(RecordManager recordManager)
    {
        recordManager.delete(getAffectedPath());
    }

    public boolean isInternal()
    {
        return internal;
    }

    public void getInvalidatedPaths(Set<String> paths)
    {
        super.getInvalidatedPaths(paths);
        paths.add(getAffectedPath());
    }
}
