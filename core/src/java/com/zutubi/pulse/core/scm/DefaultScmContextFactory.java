package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.config.ScmConfiguration;

import java.io.File;
import java.io.IOException;

/**
 * Default implementation of the scm context factory interface.
 */
public class DefaultScmContextFactory implements ScmContextFactory
{
    private File projectsDir;

    public ScmContext createContext(long id, ScmConfiguration config) throws ScmException
    {
        try
        {
            ScmContext context = new ScmContext();

            File projectDir = new File(projectsDir, String.valueOf(id));
            File workingDir = new File(projectDir, "scm");
            if (!workingDir.isDirectory() && !workingDir.mkdirs())
            {
                throw new IOException("Failed to create persistent working directory '" + workingDir.getCanonicalPath() + "'");
            }
            context.setPersistentWorkingDir(workingDir);
            return context;
            
        }
        catch (IOException e)
        {
            throw new ScmException("IO Failure creating scm context. " + e.getMessage(), e);
        }
    }

    public void setProjectsDir(File projectsDir)
    {
        this.projectsDir = projectsDir;
    }
}
