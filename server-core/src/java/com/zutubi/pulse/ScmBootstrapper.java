package com.zutubi.pulse;

import com.zutubi.pulse.core.InitialBootstrapper;
import com.zutubi.pulse.core.PulseException;
import com.zutubi.pulse.core.RecipePaths;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.scm.SCMServer;

import java.io.File;

/**
 * A bootstrapper that populates the working directory by checking out from one SCM.
 * 
 */
public abstract class ScmBootstrapper implements InitialBootstrapper
{
    protected Scm scm;

    protected Revision revision;

    public ScmBootstrapper(Scm scm)
    {
        this.scm = scm;
    }

    public void prepare() throws PulseException
    {
        // If we do have not yet have one, get the revision.
        if (revision == null)
        {
            SCMServer server = scm.createServer();
            revision = server.getLatestRevision();
        }
    }

    public void bootstrap(RecipePaths paths)
    {
        File workDir;

        if (scm.getPath() != null)
        {
            workDir = new File(paths.getBaseDir(), scm.getPath());
        }
        else
        {
            workDir = paths.getBaseDir();
        }
        bootstrap(workDir);
    }

    public Revision getRevision()
    {
        return revision;
    }

    abstract void bootstrap(File workDir);

}
