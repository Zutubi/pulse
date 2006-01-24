package com.cinnamonbob.model;

/**
 * Implementers of this interface define a policy that determines if and when
 * a BuildResult should be cleaned up.
 */
public interface BuildResultCleanupPolicy
{
    /**
     * Returns true iff the given build result can have its work directory
     * cleaned up now according to this policy.
     *
     * @param result the result to test
     * @return true iff the work directory can be cleaned up
     */
    boolean canCleanupWorkDir(BuildResult result);

    /**
     * Returns true iff the given build result can be completely cleaned up
     * now according to this policy.
     *
     * @param result the result to test
     * @return true iff the build result can be cleaned up
     */
    boolean canCleanupResult(BuildResult result);
}
