package com.zutubi.pulse.master.build.control;

import com.zutubi.pulse.master.model.BuildResult;

/**
 * The build controller interface is designed to provide an abstraction between
 * the build management and the build processing system.
 *
 * The primary purpose is to allow the management system to be tested in isolation
 * of the actual building.
 */
public interface BuildController
{
    /**
     * Get the build result id associated with this build or -1 if no result
     * is available.
     * 
     * @return build result id.
     *
     * @see BuildController#isBuildPersistent()
     */
    long getBuildResultId();

    /**
     * Returns the build number of the associated build request, or -1 if no build
     * number has been assigned.
     *
     * @return the results build number or -1. 
     *
     * @see BuildController#isBuildPersistent() 
     */
    long getBuildNumber();

    /**
     * @return true if the build controllers build result is persistent, false
     * otherwise.
     */
    boolean isBuildPersistent();

    /**
     * Start the build.
     *
     * Note that the actual build execution should not occur on the callers
     * thread else the scheduling will be blocked whilst the build is running.
     *
     * @return the build number for the controlled build
     */
    long start();
}
