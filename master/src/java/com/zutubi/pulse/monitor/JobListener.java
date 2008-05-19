package com.zutubi.pulse.monitor;

/**
 *
 *
 */
public interface JobListener
{
    void taskStarted(Task task);

    void taskFailed(Task task);

    void taskCompleted(Task task);

    void taskAborted(Task task);
}
