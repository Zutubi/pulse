package com.zutubi.tove.config.cleanup;

import com.zutubi.tove.type.record.*;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Predicate;

/**
 * A record cleanup task that removes a reference from a collection.
 */
public class RemoveReferenceCleanupTask extends RecordCleanupTaskSupport
{
    private String deletedHandle;
    private String[] inheritedReferences;

    public RemoveReferenceCleanupTask(String referencingPath, long deletedHandle, String[] inheritedReferences)
    {
        super(referencingPath);
        this.deletedHandle = Long.toString(deletedHandle);
        this.inheritedReferences = inheritedReferences;
    }

    public boolean run(RecordManager recordManager)
    {
        // Note our referencing path is for the collection holding the
        // reference.  We seek within the collection and destroy the deleted
        // handle, which may not exist at this level (it may be an inherited
        // reference).
        String parentPath = PathUtils.getParentPath(getAffectedPath());
        String baseName = PathUtils.getBaseName(getAffectedPath());

        Record parentRecord = recordManager.select(parentPath);
        if (parentRecord != null)
        {
            String[] references = (String[]) parentRecord.get(baseName);
            if (references != null && CollectionUtils.contains(references, deletedHandle))
            {
                MutableRecord newValues = parentRecord.copy(false, true);
                String[] newReferences = filter(references);
                String[] newInheritedReferences = filter(inheritedReferences);

                if (RecordUtils.valuesEqual(newReferences, newInheritedReferences))
                {
                    newValues.remove(baseName);
                }
                else
                {
                    newValues.put(baseName, newReferences);
                }
                
                recordManager.update(parentPath, newValues);
                return true;
            }
        }
        
        return false;
    }

    private String[] filter(String[] references)
    {
        if (references == null)
        {
            return null;
        }
        else
        {
            return CollectionUtils.filterToArray(references, new Predicate<String>()
            {
                public boolean satisfied(String handle)
                {
                    return !handle.equals(deletedHandle);
                }
            });
        }
    }

    public CleanupAction getCleanupAction()
    {
        return CleanupAction.PARENT_UPDATE;
    }
}
