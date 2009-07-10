package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.type.record.MutableRecord;
import com.zutubi.tove.type.record.PathUtils;
import com.zutubi.tove.type.record.Record;
import com.zutubi.tove.type.record.RecordManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

/**
 * A record cleanup task that removes a reference from a collection.
 */
public class RemoveReferenceCleanupTask extends RecordCleanupTaskSupport
{
    private String deletedHandle;
    private RecordManager recordManager;

    public RemoveReferenceCleanupTask(String referencingPath, String deletedPath, RecordManager recordManager)
    {
        super(referencingPath);
        Record deletedRecord = recordManager.select(deletedPath);
        deletedHandle = Long.toString(deletedRecord.getHandle());
        this.recordManager = recordManager;
    }

    public void run()
    {
        // Note our referencing path is of the form:
        //   <referencing collection>/<index>.
        // Due to templating, the index isn't much use.  Instead, we seek and
        // destroy the deleted handle, which may not exist at this level (it
        // may be an inherited reference).
        String collectionPath = PathUtils.getParentPath(getAffectedPath());
        String parentPath = PathUtils.getParentPath(collectionPath);
        String baseName = PathUtils.getBaseName(collectionPath);

        Record parentRecord = recordManager.select(parentPath);
        if (parentRecord != null)
        {
            String[] references = (String[]) parentRecord.get(baseName);
            if (references != null && CollectionUtils.contains(references, deletedHandle))
            {
                MutableRecord newValues = parentRecord.copy(false);
                String[] newReferences = CollectionUtils.filterToArray(references, new Predicate<String>()
                {
                    public boolean satisfied(String handle)
                    {
                        return !handle.equals(deletedHandle);
                    }
                });

                newValues.put(baseName, newReferences);
                recordManager.update(parentPath, newValues);
            }
        }
    }
}
