package com.zutubi.pulse.master;

import com.zutubi.pulse.core.scm.api.Revision;

/**
 * The build handler interface is designed to provide an abstraction between
 * the build scheduling and the build processing system.
 *
 * The primary purpose is to allow the scheduling system to be tested in isolation
 * of the actual building.
 */
public interface BuildHandler
{
    /**
     * Update the build revision associated with the build if
     * that revision has not yet been fixed.  Return true if the
     * revision was updated, false otherwise.
     *
     * @param revision the revision to update to.
     *
     * @return true if the handled build revision was updated, false otherwise.
     */
    boolean updateRevisionIfNotFixed(Revision revision);

    /**
     * Get the build result id associated with this build.
     * @return build result id.
     */
    long getBuildResultId();

    /**
     * Run the build.
     *
     * Note that the actual build execution should not occur on the callers
     * thread else the scheduling will be blocked whilst the build is running.
     */
    void run();
}
