package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMServer;
import com.zutubi.pulse.scm.SCMServerUtils;
import com.zutubi.pulse.util.logging.Logger;

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

    SCMServer bootstrap(File workDir)
    {
        SCMServer server = null;
        try
        {
            server = scm.createServer();
            server.update(getId(), workDir, revision.getRevision(), this);
            return server;
        }
        catch (SCMException e)
        {
            SCMServerUtils.close(server);
            LOG.severe(e);
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }
}
