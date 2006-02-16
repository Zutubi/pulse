package com.cinnamonbob;

import com.cinnamonbob.core.Bootstrapper;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.RecipePaths;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.model.Scm;
import com.cinnamonbob.scm.SCMException;

import java.io.File;

/**
 * A bootstrapper that populates the working directory by checking out from one
 * or more SCMs.
 */
public class ScmBootstrapper implements Bootstrapper
{
    public Scm scm;
    public Revision revision;

    public ScmBootstrapper(Scm scm)
    {
        this.scm = scm;
    }

    public void bootstrap(long recipeId, RecipePaths paths)
    {
        File checkoutDir;

        if (scm.getPath() != null)
        {
            checkoutDir = new File(paths.getBaseDir(), scm.getPath());
        }
        else
        {
            checkoutDir = paths.getBaseDir();
        }

        try
        {
            scm.createServer().checkout(recipeId, checkoutDir, revision, null);
        }
        catch (SCMException e)
        {
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }

    public Revision getRevision()
    {
        return revision;
    }

    public void setRevision(Revision revision)
    {
        this.revision = revision;
    }

}
