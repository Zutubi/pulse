package com.zutubi.pulse.master.monitor;

import java.util.Iterator;

/**
 *
 *
 */
public interface Job<T extends Task>
{
    Iterator<T> getTasks();
}
