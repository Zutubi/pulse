package com.zutubi.pulse.monitor;

import java.util.Iterator;

/**
 *
 *
 */
public interface Job<T extends Task>
{
    Iterator<T> getTasks();
}
