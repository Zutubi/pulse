package com.zutubi.pulse.master.util.monitor;

import java.util.List;

/**
 * The task interface represents the smallest unit of work that can be monitored
 * by the monitoring system.
 */
public interface Task
{
    /**
     * Get a human readable name of this upgrade task. This name must uniquely identify this
     * task.
     *
     * @return the task name.
     */
    String getName();

    /**
     * Get a human readable description of what this task does. This will be
     * displayed to the user to inform them of what is happening.
     *
     * @return a short descriptive message
     */
    String getDescription();

    /**
     * The list of errors that this task encountered during execution.
     *
     * @return a list of error messages
     */
    List<String> getErrors();

    /**
     * If this tasks fails (generates an exception during execution) then the processing
     * will be aborted if halt on failure is true.
     *
     * For example: If this upgrade task requires a database connection and non is available
     * for whatever reason, then halt on failure should be true. Once the connection is available
     * then we can continue.
     *
     * @return boolean indicating whether or not to halt processing on task failure.
     */
    boolean haltOnFailure();

    /**
     * Indicates whether or not the processing of this task has failed.
     *
     * @return true if the task has failed, false otherwise.
     */
    boolean hasFailed();

    /**
     * Run this upgrade task.
     *
     * @throws TaskException if a problem occured during task execution.
     */
    void execute() throws TaskException;
}
