package com.zutubi.pulse.master.util.monitor;

import java.util.Arrays;
import java.util.Iterator;

public class ArrayJobWrapper<T extends Task> implements Job<T>
{
    private T[] tasks;

    public ArrayJobWrapper(T... tasks)
    {
        this.tasks = tasks;
    }

    public Iterator<T> iterator()
    {
        return Arrays.asList(tasks).iterator();
    }
}
