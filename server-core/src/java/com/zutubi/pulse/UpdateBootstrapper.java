package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.scm.config.ScmConfiguration;
import com.zutubi.pulse.scm.ScmClient;
import com.zutubi.util.logging.Logger;

import java.io.File;

/**
 */
public class UpdateBootstrapper extends ScmBootstrapper
{
    private static final Logger LOG = Logger.getLogger(UpdateBootstrapper.class);

    public UpdateBootstrapper(String project, ScmClient client, BuildRevision revision)
    {
        super(project, client, revision);
    }

    ScmClient bootstrap(File workDir)
    {
        try
        {
            scm.update(getId(), workDir, revision.getRevision(), this);
            return scm;
        }
        catch (ScmException e)
        {
            LOG.severe(e);
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }
}
