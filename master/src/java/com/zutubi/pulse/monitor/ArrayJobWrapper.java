package com.zutubi.pulse.monitor;

import java.util.Iterator;
import java.util.Arrays;

/**
 *
 *
 */
public class ArrayJobWrapper implements Job
{
    private Task[] tasks;

    public ArrayJobWrapper(Task... tasks)
    {
        this.tasks = tasks;
    }

    public Iterator<Task> getTasks()
    {
        return Arrays.asList(tasks).iterator();
    }
}
