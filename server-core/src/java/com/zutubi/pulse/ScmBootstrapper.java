package com.zutubi.pulse;

import com.zutubi.pulse.core.*;
import com.zutubi.pulse.core.model.Revision;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.scm.SCMServer;

import java.io.File;

/**
 * A bootstrapper that populates the working directory by checking out from one SCM.
 * 
 */
public abstract class ScmBootstrapper implements Bootstrapper
{
    protected Scm scm;
    protected BuildRevision revision;

    public ScmBootstrapper(Scm scm, BuildRevision revision)
    {
        this.scm = scm;
        this.revision = revision;
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

    abstract void bootstrap(File workDir);

}
