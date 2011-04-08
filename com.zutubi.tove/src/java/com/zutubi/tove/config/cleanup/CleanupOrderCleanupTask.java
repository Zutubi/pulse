package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.type.CollectionType;
import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;

import java.util.List;

/**
 * A reference cleanup task that ensures a declared order on a collection has
 * the deleted key removed - if such an order exists and has the key.
 */
public class CleanupOrderCleanupTask extends RecordCleanupTaskSupport
{
    public CleanupOrderCleanupTask(String path)
    {
        super(path);
    }

    public boolean run(RecordManager recordManager)
    {
        String parentPath = PathUtils.getParentPath(getAffectedPath());
        String baseName = PathUtils.getBaseName(getAffectedPath());
        Record parentRecord = recordManager.select(parentPath);

        if(parentRecord != null)
        {
            List<String> order = CollectionType.getDeclaredOrder(parentRecord);
            if(order.remove(baseName))
            {
                MutableRecord mutableParent = parentRecord.copy(false, true);
                CollectionType.setOrder(mutableParent, order);
                recordManager.update(parentPath, mutableParent);
                return true;
            }
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
}
