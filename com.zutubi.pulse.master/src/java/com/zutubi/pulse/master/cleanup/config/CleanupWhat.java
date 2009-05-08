package com.zutubi.pulse.master.cleanup.config;

/**
 * The cleanup what defines the options of what part of a build
 * can be cleaned up separately.
 */
public enum CleanupWhat
{
    /**
     * Cleanup the working directories if retained.
     */
    WORKING_DIRECTORIES_ONLY,
    /**
     * Cleanup the build directories and captured artifacts
     */
    BUILD_ARTIFACTS,
    /**
     * Delete the build from the database, along with any of
     * the artifacts published to the repository.
     */
    WHOLE_BUILDS
}
