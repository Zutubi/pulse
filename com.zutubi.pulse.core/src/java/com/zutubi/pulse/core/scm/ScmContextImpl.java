package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.api.ScmContext;

import java.io.File;

/**
 * A simple implementation of {@link com.zutubi.pulse.core.scm.api.ScmContext}.
 */
public class ScmContextImpl implements ScmContext
{
    private File persistentWorkingDir;

    public File getPersistentWorkingDir()
    {
        return persistentWorkingDir;
    }

    public void setPersistentWorkingDir(File dir)
    {
        this.persistentWorkingDir = dir;
    }
}
