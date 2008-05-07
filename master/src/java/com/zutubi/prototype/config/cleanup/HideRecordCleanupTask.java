package com.zutubi.prototype.config.cleanup;

import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.prototype.type.record.TemplateRecord;

import java.util.Set;

/**
 * A reference cleanup task that hides an inherited record.  This is the same
 * as deleting the record, but additionally adds the required hidden key to
 * the parent record.  Any tasks needed to cleanup references to the deleted
 * record will cascade off this.
 */
public class HideRecordCleanupTask extends RecordCleanupTaskSupport
{
    private long handle;
    private boolean internal;
    private RecordManager recordManager;

    public HideRecordCleanupTask(long handle, String path, boolean internal, RecordManager recordManager)
    {
        super(path);
        this.handle = handle;
        this.internal = internal;
        this.recordManager = recordManager;
    }

    public void run()
    {
        recordManager.delete(getAffectedPath());

        String parentPath = PathUtils.getParentPath(getAffectedPath());
        MutableRecord parent = recordManager.select(parentPath).copy(false);
        TemplateRecord.hideItem(parent, handle);
        recordManager.update(parentPath, parent);
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
