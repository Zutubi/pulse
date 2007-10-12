package com.zutubi.prototype.config.cleanup;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Helper base class for implementations of {@link RecordCleanupTask}.
 */
public abstract class RecordCleanupTaskSupport implements RecordCleanupTask
{
    private String path;
    private List<RecordCleanupTask> cascaded = new LinkedList<RecordCleanupTask>();

    public RecordCleanupTaskSupport(String path)
    {
        this.path = path;
    }

    public String getAffectedPath()
    {
        return path;
    }

    public List<RecordCleanupTask> getCascaded()
    {
        return cascaded;
    }

    @SuppressWarnings({"unchecked"})
    public void getInvalidatedPaths(Set<String> paths)
    {
        for(RecordCleanupTask cascade: cascaded)
        {
            cascade.getInvalidatedPaths(paths);
        }
    }

    public boolean isAsynchronous()
    {
        return false;
    }

    public boolean isInternal()
    {
        return false;
    }

    public void addCascaded(RecordCleanupTask task)
    {
        cascaded.add(task);
    }
}
