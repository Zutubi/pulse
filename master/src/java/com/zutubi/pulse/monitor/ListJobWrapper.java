package com.zutubi.pulse.monitor;

import java.util.Iterator;
import java.util.List;

/**
 *
 *
 */
public class ListJobWrapper<T extends Task> implements Job<T>
{
    private List<T> tasks;

    public ListJobWrapper(List<T> tasks)
    {
        this.tasks = tasks;
    }

    public Iterator<T> getTasks()
    {
        return tasks.iterator();
    }
}