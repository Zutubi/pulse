package com.zutubi.pulse.monitor;

/**
 *
 *
 */
public interface JobListener<T extends Task>
{
    void taskStarted(T task);

    void taskFailed(T task);

    void taskCompleted(T task);

    void taskAborted(T task);
}
