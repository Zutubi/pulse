package com.zutubi.pulse.master.monitor;

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
