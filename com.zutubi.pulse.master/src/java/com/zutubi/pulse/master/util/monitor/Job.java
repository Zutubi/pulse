package com.zutubi.pulse.master.util.monitor;

import java.util.Iterator;

/**
 * A job represents a group of tasks that are executed together.
 */
public interface Job<T extends Task>
{
    Iterator<T> getTasks();
}
