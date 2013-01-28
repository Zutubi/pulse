package com.zutubi.pulse.master.util.monitor;

import java.util.Iterator;
import java.util.List;

public class ListJobWrapper<T extends Task> implements Job<T>
{
    private List<T> tasks;

    public ListJobWrapper(List<T> tasks)
    {
        this.tasks = tasks;
    }

    public Iterator<T> iterator()
    {
        return tasks.iterator();
    }
}