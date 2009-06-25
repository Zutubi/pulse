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
    WORKING_COPY_SNAPSHOT,
    
    /**
     * Cleanup the build directories and captured artifacts.
     */
    BUILD_ARTIFACTS,

    /**
     * Cleanup the artifacts published to the repository.
     */
    REPOSITORY_ARTIFACTS,

    /**
     * Cleanup the build logs.
     */
    LOGS
}
