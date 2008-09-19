package com.zutubi.pulse.core.scm;

import java.io.File;

/**
 * The context in which scm operations executed outside of the build are run.
 * 
 */
public class ScmContext
{
    private File persistentWorkingDir;

    /**
     * @return location to use for the working copy.
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
