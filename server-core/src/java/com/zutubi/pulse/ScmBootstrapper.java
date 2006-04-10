package com.zutubi.pulse;

import com.zutubi.pulse.core.BobException;
import com.zutubi.pulse.core.Bootstrapper;
import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMServer;

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

    public void prepare() throws BobException
    {
        // If we do have not yet have one, get the revision.
        if (revision == null)
        {
            SCMServer server = scm.createServer();
            revision = server.getLatestRevision();
        }
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
