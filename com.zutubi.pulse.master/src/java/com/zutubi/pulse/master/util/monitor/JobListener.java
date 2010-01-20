package com.zutubi.pulse.master.util.monitor;

/**
 * The job listener interface defines a set of callbacks that can be
 * implemented to track the execution of the tasks within a job.
 */
public interface JobListener<T extends Task>
{
    /**
     * Callback triggered when the execution of a task is started.
     *
     * @param task      the task being started
     * @param feedback  the feedback associated with the task
     */
    void taskStarted(T task, TaskFeedback<T> feedback);

    /**
     * Callback triggerd when the execution of a task fails.
     *
     * @param task      the task that failed
     * @param feedback  the feedback associated with the task
     */
    void taskFailed(T task, TaskFeedback<T> feedback);

    /**
     * Callback triggered when the execution of a task completes.
     *
     * @param task      the task that was completed
     * @param feedback  the feedback associated with the task
     */
    void taskCompleted(T task, TaskFeedback<T> feedback);

    /**
     * Callback triggered when the execution of a task is aborted.
     *
     * @param task      the task that was aborted
     * @param feedback  the feedback associated with the task
     */
    void taskAborted(T task, TaskFeedback feedback);
}
