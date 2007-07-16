package com.zutubi.pulse.upgrade;

import java.util.List;

/**
 * The upgrade task interface.  All upgrade tasks need to implement this interface.
 *
 */
public interface UpgradeTask
{
    /**
     * Get a human readable name of this upgrade task. This name must uniquely identify this
     * task.
     *
     * @return the upgrade task name.
     */
    String getName();

    /**
     * Get a human readable description of what this upgrade task does. This will be
     * displayed to the user to inform them of what is happening.
     *
     * @return a short descriptive message
     */
    String getDescription();

    /**
     * Return the build number that will trigger this upgrade task.
     *
     * For example: If the setting this to 301 will cause this task to
     * be executed on all installations with build version 300 and below (inclusive).
     *
     * @return the build number that triggers this upgrade task
     */
    int getBuildNumber();

    /**
     * Run this upgrade task.
     *
     * @param context provides information about the upgrade.
     *
     * @throws UpgradeException on error
     */
    void execute(UpgradeContext context) throws UpgradeException;

    /**
     * The list of errors that this upgrade task encountered during execution.
     *
     * @return the list of errors messages generated during the execution of this upgrade task. 
     */
    List<String> getErrors();

    /**
     * If this upgrade tasks fails (generates an exception during execution) then the upgrade
     * will be aborted if halt on failure is true.
     *
     * For example: If this upgrade task requires a database connection and non is available
     * for whatever reason, then halt on failure should be true. Once the connection is available
     * then we can continue.
     *
     * @return true if the upgrade should halt if this upgrade fails.
     */
    boolean haltOnFailure();

    /**
     * Indicates whether or not the processing of this upgrade task has failed.
     *
     * @return true if the task has failed, false otherwise.
     */
    boolean hasFailed();
}
