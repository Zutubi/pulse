package com.zutubi.pulse.core.scm.api;

import java.io.File;

/**
 * The context in which scm operations executed outside of the build are run.
 * 
 */
public class ScmContext
{
    private File persistentWorkingDir;

    /**
     * @return a persistent working directory available for use by the scm
     * implementation to persist data between invocations.
     */
    public File getPersistentWorkingDir()
    {
        return persistentWorkingDir;
    }

    public void setPersistentWorkingDir(File dir)
    {
        this.persistentWorkingDir = dir;
    }

}
