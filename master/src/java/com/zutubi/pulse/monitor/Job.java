package com.zutubi.pulse.monitor;

import java.util.Iterator;

/**
 *
 *
 */
public interface Job
{
    Iterator<Task> getTasks();
}
