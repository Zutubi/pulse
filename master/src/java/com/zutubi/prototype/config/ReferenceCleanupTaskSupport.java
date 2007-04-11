package com.zutubi.prototype.config;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Helper base class for implementations of {@link ReferenceCleanupTask}.
 */
public abstract class ReferenceCleanupTaskSupport implements ReferenceCleanupTask
{
    private String path;
    private List<ReferenceCleanupTask> cascaded = new LinkedList<ReferenceCleanupTask>();

    public ReferenceCleanupTaskSupport(String path)
    {
        this.path = path;
    }

    public String getAffectedPath()
    {
        return path;
    }

    public List<ReferenceCleanupTask> getCascaded()
    {
        return cascaded;
    }

    @SuppressWarnings({"unchecked"})
    public void getInvalidatedPaths(Set<String> paths)
    {
        for(ReferenceCleanupTask cascade: cascaded)
        {
            cascade.getInvalidatedPaths(paths);
        }
    }

    public void execute()
    {
        for(ReferenceCleanupTask cascade: cascaded)
        {
            cascade.execute();
        }
    }

    public void addCascaded(ReferenceCleanupTask task)
    {
        cascaded.add(task);
    }
}
