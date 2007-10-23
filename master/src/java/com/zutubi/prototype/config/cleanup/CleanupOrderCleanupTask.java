package com.zutubi.prototype.config.cleanup;

import com.zutubi.prototype.type.record.*;
import com.zutubi.prototype.type.CollectionType;

import java.util.Set;
import java.util.List;

/**
 * A reference cleanup task that ensures a declared order on a collection has
 * the deleted key removed - if such an order exists and has the key.
 */
public class CleanupOrderCleanupTask extends RecordCleanupTaskSupport
{
    private RecordManager recordManager;

    public CleanupOrderCleanupTask(String path, RecordManager recordManager)
    {
        super(path);
        this.recordManager = recordManager;
    }

    public void run()
    {
        String parentPath = PathUtils.getParentPath(getAffectedPath());
        String baseName = PathUtils.getBaseName(getAffectedPath());
        Record parentRecord = recordManager.select(parentPath);

        if(parentRecord != null)
        {
            List<String> order = CollectionType.getDeclaredOrder(parentRecord);
            if(order.remove(baseName))
            {
                MutableRecord mutableParent = parentRecord.copy(false);
                CollectionType.setOrder(mutableParent, order);
                recordManager.update(parentPath, mutableParent);
            }
        }
    }

    public boolean isInternal()
    {
        return true;
    }
}
