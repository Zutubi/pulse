package com.cinnamonbob;

import com.cinnamonbob.core.Bootstrapper;
import com.cinnamonbob.core.BuildException;
import com.cinnamonbob.core.RecipePaths;
import com.cinnamonbob.core.model.Change;
import com.cinnamonbob.core.model.Revision;
import com.cinnamonbob.model.Scm;
import com.cinnamonbob.scm.SCMException;

import java.io.File;
import java.util.LinkedList;

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
            checkoutDir = new File(paths.getWorkDir(), scm.getPath());
        }
        else
        {
            checkoutDir = paths.getWorkDir();
        }

        try
        {
            // TODO this list is not needed, perhaps make it optional in SCM interface?
            scm.createServer().checkout(recipeId, checkoutDir, revision, new LinkedList<Change>());
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
