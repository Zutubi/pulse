package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMClient;
import com.zutubi.util.logging.Logger;

import java.io.File;

/**
 */
public class UpdateBootstrapper extends ScmBootstrapper
{
    private static final Logger LOG = Logger.getLogger(UpdateBootstrapper.class);

    public UpdateBootstrapper(String project, String spec, Scm scm, BuildRevision revision)
    {
        super(project, spec, scm, revision);
    }

    SCMClient bootstrap(File workDir)
    {
        try
        {
            SCMClient client = scm.createServer();
            client.update(getId(), workDir, revision.getRevision(), this);
            return client;
        }
        catch (SCMException e)
        {
            LOG.severe(e);
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }
}
