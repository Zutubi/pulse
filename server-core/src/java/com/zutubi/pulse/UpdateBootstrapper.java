package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.scm.SCMException;

import java.io.File;

/**
 * <class-comment/>
 */
public class UpdateBootstrapper extends ScmBootstrapper
{
    public UpdateBootstrapper(Scm scm, BuildRevision revision)
    {
        super(scm, revision);
    }

    void bootstrap(File workDir)
    {
        try
        {
            scm.createServer().update(workDir, revision.getRevision());
        }
        catch (SCMException e)
        {
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }
}
