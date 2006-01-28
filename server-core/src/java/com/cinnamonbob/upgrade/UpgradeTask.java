package com.cinnamonbob.upgrade;

import java.util.List;

/**
 * <class-comment/>
 */
public interface UpgradeTask
{
    /**
     * Return the build number that will trigger this upgrade task.
     */
    int getBuildNumber();

    /**
     * Run this upgrade task.
     *
     * @param context provides information about the upgrade.
     *
     * @throws UpgradeException
     */
    void execute(UpgradeContext context) throws UpgradeException;

    /**
     * Get a human readable description of what this upgrade task does. This will be
     * displayed to the user to inform them of what is happening.
     *
     * @return a short descriptive message
     */
    String getDescription();

    //---------------
    // The following methods require a little bit more thought. How would we display the
    // errors? Under what sort of failure conditions will halting still leave the
    // application in a stable state? (resource issues...)

    /**
     * The list of errors that this upgrade task encountered during execution.
     *
     */
    List<String> getErrors();

    /**
     * If this upgrade tasks fails (generates an exception during execution) then the upgrade
     * will be aborted if halt on failure is true.
     *
     * For example: If this upgrade task requires a database connection and non is available
     * for whatever reason, then halt on failure should be true. Once the connection is available
     * then we can continue.
     */
    boolean haltOnFailure();
}
