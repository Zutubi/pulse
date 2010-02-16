package com.zutubi.pulse.master.build.control;

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
     * Get the build result id associated with this build.
     * @return build result id.
     */
    long getBuildResultId();

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
