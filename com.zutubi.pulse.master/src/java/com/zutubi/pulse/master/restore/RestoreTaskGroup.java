package com.zutubi.pulse.master.restore;

import java.util.LinkedList;
import java.util.List;

/**
 *
 *
 */
public class RestoreTaskGroup
{
    private ArchiveableComponent source;

    private List<RestoreTask> tasks = new LinkedList<RestoreTask>();

    public ArchiveableComponent getSource()
    {
        return source;
    }

    public void setSource(ArchiveableComponent source)
    {
        this.source = source;
    }

    public List<RestoreTask> getTasks()
    {
        return tasks;
    }

    public void setTasks(List<RestoreTask> tasks)
    {
        this.tasks = tasks;
    }

    public void addTask(RestoreComponentTask task)
    {
        tasks.add(task);
    }
}
