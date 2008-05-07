package com.zutubi.prototype.config.cleanup;

import com.zutubi.prototype.type.record.*;

import java.util.Set;

/**
 * A reference cleanup task that cleans up a hidden key for a record.  Note
 * that the hidden key may not always be present: if the parent itself is
 * hidden, for example.
 */
public class CleanupHiddenKeyCleanupTask extends RecordCleanupTaskSupport
{
    private RecordManager recordManager;
    private long handle;

    public CleanupHiddenKeyCleanupTask(long handle, String path, RecordManager recordManager)
    {
        super(path);
        this.handle = handle;
        this.recordManager = recordManager;
    }

    public void run()
    {
        String parentPath = PathUtils.getParentPath(getAffectedPath());
        Record parent = recordManager.select(parentPath);
        if(parent != null && TemplateRecord.getHiddenHandles(parent).contains(handle))
        {
            MutableRecord mutableParent = parent.copy(false);
            TemplateRecord.restoreItem(mutableParent, handle);
            recordManager.update(parentPath, mutableParent);
        }
    }

    public boolean isInternal()
    {
        return true;
    }

    @SuppressWarnings({"unchecked"})
    public void getInvalidatedPaths(Set<String> paths)
    {
        super.getInvalidatedPaths(paths);
    }
}
