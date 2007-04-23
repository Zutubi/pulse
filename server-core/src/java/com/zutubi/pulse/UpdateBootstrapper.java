package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.servercore.config.ScmConfiguration;
import com.zutubi.pulse.servercore.scm.ScmClient;
import com.zutubi.util.logging.Logger;

import java.io.File;

/**
 */
public class UpdateBootstrapper extends ScmBootstrapper
{
    private static final Logger LOG = Logger.getLogger(UpdateBootstrapper.class);

    public UpdateBootstrapper(String project, String spec, ScmConfiguration scm, BuildRevision revision)
    {
        super(project, spec, scm, revision);
    }

    ScmClient bootstrap(File workDir)
    {
        try
        {
            ScmClient client = scm.createClient();
            client.update(getId(), workDir, revision.getRevision(), this);
            return client;
        }
        catch (ScmException e)
        {
            LOG.severe(e);
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }
}
