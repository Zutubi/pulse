package com.zutubi.pulse.core.scm;

import com.zutubi.pulse.core.scm.config.ScmConfiguration;

import java.io.File;
import java.io.IOException;

public class DefaultScmContextFactory implements ScmContextFactory
{
    // would like access to the ConfigurationProvider here, but it is in master, and we are in core.
    // would be nice to be able to 'lookup' the project configuration given the scm configuration, and
    // determine the details from there.

    private File projectsDir;

    public ScmContext createContext(long id, ScmConfiguration config) throws ScmException
    {
        // OPTIONS:
        // a) DATA/projects/projectConfig.getProjectId()/scm/xxxxxxx
        // b) DATA/work/<projectname>/.scm...
        // going with a for now.
        
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
            throw new ScmException("IO Failure creating scm context. " + e.getMessage());
        }
    }

    public void setProjectsDir(File projectsDir)
    {
        this.projectsDir = projectsDir;
    }
}
