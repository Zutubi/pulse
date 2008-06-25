package com.zutubi.pulse.monitor;

import java.util.List;
import java.util.Iterator;

/**
 *
 *
 */
public class ListJobWrapper implements Job
{
    private List<Task> tasks;

    public ListJobWrapper(List<Task> tasks)
    {
        this.tasks = tasks;
    }

    public Iterator<Task> getTasks()
    {
        return tasks.iterator();
    }
}