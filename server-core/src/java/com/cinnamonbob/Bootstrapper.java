package com.cinnamonbob;

import java.io.File;

/**
 */
public interface Bootstrapper
{
    /**
     * Bootstraps a working directory at the given location.  The directory
     * should not currently exist.
     *
     * @param workDir the directory to bootstrap to (created during bootstrap)
     */
    void bootstrap(File workDir);
}
