package com.zutubi.pulse.master.util.monitor;

public enum TaskStatus
{
    /**
     * The task has been initialised and is waiting to be executed.
     */
    PENDING,
    /**
     * The task is currently in progress.
     */
    IN_PROGRESS,
    /**
     * The task completed successfully.
     */
    SUCCESS,
    /**
     * The task failed to complete.
     */
    FAILED,
    /**
     * The task was aborted before it was executed.
     */
    ABORTED
}