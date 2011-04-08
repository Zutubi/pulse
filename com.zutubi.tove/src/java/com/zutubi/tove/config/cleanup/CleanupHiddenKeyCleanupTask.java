package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.type.record.*;

import java.util.Set;

/**
 * A reference cleanup task that cleans up a hidden key for a record.  Note
 * that the hidden key may not always be present: if the parent itself is
 * hidden, for example.
 */
public class CleanupHiddenKeyCleanupTask extends RecordCleanupTaskSupport
{
    public CleanupHiddenKeyCleanupTask(String path)
    {
        super(path);
    }

    public boolean run(RecordManager recordManager)
    {
        String parentPath = PathUtils.getParentPath(getAffectedPath());
        String baseName = PathUtils.getBaseName(getAffectedPath());

        Record parent = recordManager.select(parentPath);
        if(parent != null && TemplateRecord.getHiddenKeys(parent).contains(baseName))
        {
            MutableRecord mutableParent = parent.copy(false, true);
            TemplateRecord.restoreItem(mutableParent, baseName);
            recordManager.update(parentPath, mutableParent);
            return true;
        }
        
        return false;
    }

    public boolean isInternal()
    {
        return true;
    }

    public CleanupAction getCleanupAction()
    {
        return CleanupAction.NONE;
    }

    public void getInvalidatedPaths(Set<String> paths)
    {
        super.getInvalidatedPaths(paths);
    }
}
