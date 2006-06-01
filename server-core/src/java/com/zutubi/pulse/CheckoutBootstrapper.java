package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.scm.SCMException;

import java.io.File;

/**
 * <class-comment/>
 */
public class CheckoutBootstrapper extends ScmBootstrapper
{
    public CheckoutBootstrapper(Scm scm)
    {
        super(scm);
    }

    void bootstrap(File workDir)
    {
        try
        {
            // the recipe id is not used by any of the scm servers, and is a detail they should
            // not need to be aware of.
            int recipeId = 0;
            scm.createServer().checkout(recipeId, workDir, getRevision(), null);
        }
        catch (SCMException e)
        {
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }
}
