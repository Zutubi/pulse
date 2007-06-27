package com.zutubi.prototype.config;

import com.zutubi.prototype.type.record.MutableRecord;
import com.zutubi.prototype.type.record.PathUtils;
import com.zutubi.prototype.type.record.Record;
import com.zutubi.prototype.type.record.RecordManager;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

/**
 * A record cleanup task that removes a reference from a collection.
 */
public class RemoveReferenceCleanupTask extends RecordCleanupTaskSupport
{
    private String deletedPath;
    private RecordManager recordManager;

    public RemoveReferenceCleanupTask(String deletedPath, String referencingPath, RecordManager recordManager)
    {
        super(referencingPath);
        this.deletedPath = deletedPath;
        this.recordManager = recordManager;
    }

    public void execute()
    {
        super.execute();
        
        String parentPath = PathUtils.getParentPath(getAffectedPath());
        String baseName = PathUtils.getBaseName(getAffectedPath());

        Record parentRecord = recordManager.load(parentPath);
        if (parentRecord != null)
        {
            MutableRecord newValues = parentRecord.copy(false);
            String[] references = (String[]) newValues.get(baseName);
            references = CollectionUtils.filterToArray(references, new Predicate<String>()
            {
                public boolean satisfied(String s)
                {
                    return !s.equals(deletedPath);
                }
            });

            if(references.length > 0)
            {
                newValues.put(baseName, references);
            }
            else
            {
                newValues.remove(baseName);
            }

            recordManager.update(parentPath, newValues);
        }
    }
}
