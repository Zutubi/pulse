package com.zutubi.pulse.master.util.monitor;

import java.util.Iterator;

/**
 * A job represents a group of tasks that are executed together.
 */
public interface Job<T extends Task>
{
    /**
     * Retrieve an iterator over the tasks contained by this job.
     *
     * @return an interator
     */
    Iterator<T> getTasks();
}
