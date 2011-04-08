package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.type.record.*;

import java.util.Set;

/**
 * A reference cleanup task that hides an inherited record.  This is the same
 * as deleting the record, but additionally adds the required hidden key to
 * the parent record.  Any tasks needed to cleanup references to the deleted
 * record will cascade off this.
 */
public class HideRecordCleanupTask extends RecordCleanupTaskSupport
{
    private boolean internal;

    public HideRecordCleanupTask(String path, boolean internal)
    {
        super(path);
        this.internal = internal;
    }

    public boolean run(RecordManager recordManager)
    {
        Record deleted = recordManager.delete(getAffectedPath());
        if (deleted == null)
        {
            return false;
        }

        String parentPath = PathUtils.getParentPath(getAffectedPath());
        String baseName = PathUtils.getBaseName(getAffectedPath());
        MutableRecord parent = recordManager.select(parentPath).copy(false, true);

        TemplateRecord.hideItem(parent, baseName);
        recordManager.update(parentPath, parent);
        return true;
    }

    public boolean isInternal()
    {
        return internal;
    }

    public CleanupAction getCleanupAction()
    {
        return CleanupAction.DELETE;
    }

    public void getInvalidatedPaths(Set<String> paths)
    {
        super.getInvalidatedPaths(paths);
        paths.add(getAffectedPath());
    }
}
