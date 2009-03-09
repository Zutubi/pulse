package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

/**
 * A record cleanup task that nulls out a reference.  Should only be used
 * when the reference is not required.
 */
public class NullifyReferenceCleanupTask extends RecordCleanupTaskSupport
{
    public NullifyReferenceCleanupTask(String referencingPath)
    {
        super(referencingPath);
    }

    public void run(RecordManager recordManager)
    {
        String parentPath = PathUtils.getParentPath(getAffectedPath());
        Record parentRecord = recordManager.select(parentPath);
        if (parentRecord != null)
        {
            MutableRecord newValue = parentRecord.copy(false);
            newValue.put(PathUtils.getBaseName(getAffectedPath()), "0");
            recordManager.update(parentPath, newValue);
        }
    }
}
