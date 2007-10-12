package com.zutubi.prototype.config.cleanup;

import com.zutubi.prototype.type.record.RecordManager;

import java.util.Set;

/**
 * A reference cleanup task that deletes a record.  Any tasks needed to
 * cleanup references to the deleted record will cascade off this.
 */
public class DeleteRecordCleanupTask extends RecordCleanupTaskSupport
{
    private boolean internal;
    private RecordManager recordManager;

    public DeleteRecordCleanupTask(String path, boolean internal, RecordManager recordManager)
    {
        super(path);
        this.internal = internal;
        this.recordManager = recordManager;
    }

    public void run()
    {
        recordManager.delete(getAffectedPath());
    }

    public boolean isInternal()
    {
        return internal;
    }

    @SuppressWarnings({"unchecked"})
    public void getInvalidatedPaths(Set<String> paths)
    {
        super.getInvalidatedPaths(paths);
        paths.add(getAffectedPath());
    }
}
