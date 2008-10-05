package com.zutubi.pulse.master.util.monitor;

/**
 * The job listener interface defines a set of callbacks that can be
 * implemented to track the execution of the tasks within a job.
 */
public interface JobListener<T extends Task>
{
    void taskStarted(T task);

    void taskFailed(T task);

    void taskCompleted(T task);

    void taskAborted(T task);
}
