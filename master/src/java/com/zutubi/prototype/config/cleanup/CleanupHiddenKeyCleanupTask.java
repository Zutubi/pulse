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

    public CleanupHiddenKeyCleanupTask(String path, RecordManager recordManager)
    {
        super(path);
        this.recordManager = recordManager;
    }

    public void run()
    {
        String parentPath = PathUtils.getParentPath(getAffectedPath());
        String baseName = PathUtils.getBaseName(getAffectedPath());

        Record parent = recordManager.select(parentPath);
        if(parent != null && TemplateRecord.getHiddenKeys(parent).contains(baseName))
        {
            MutableRecord mutableParent = parent.copy(false);
            TemplateRecord.hideItem(mutableParent, baseName);
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
