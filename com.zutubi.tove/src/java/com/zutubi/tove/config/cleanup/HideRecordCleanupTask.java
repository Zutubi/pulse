package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.tove.type.record.TemplateRecord;

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
    private RecordManager recordManager;

    public HideRecordCleanupTask(String path, boolean internal, RecordManager recordManager)
    {
        super(path);
        this.internal = internal;
        this.recordManager = recordManager;
    }

    public void run()
    {
        recordManager.delete(getAffectedPath());

        String parentPath = PathUtils.getParentPath(getAffectedPath());
        String baseName = PathUtils.getBaseName(getAffectedPath());
        MutableRecord parent = recordManager.select(parentPath).copy(false);

        TemplateRecord.hideItem(parent, baseName);
        recordManager.update(parentPath, parent);
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
