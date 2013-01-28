package com.zutubi.pulse.master.util.monitor;

/**
 * A job represents a group of tasks that are executed together.
 */
public interface Job<T extends Task> extends Iterable<T>
{
}
