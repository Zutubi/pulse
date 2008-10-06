package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.ExecutionContext;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.ScmClientFactory;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.util.logging.Logger;

/**
 * Bootstrapper which runs an incremental update on a working copy using the
 * project's SCM.
 */
public class UpdateBootstrapper extends ScmBootstrapper
{
    private static final Logger LOG = Logger.getLogger(UpdateBootstrapper.class);

    public UpdateBootstrapper(String project, ScmConfiguration scmConfig, BuildRevision revision, ScmClientFactory factory)
    {
        super(project, scmConfig, revision, factory);
    }

    ScmClient doBootstrap(ExecutionContext executionContext)
    {
        ScmClient scm = null;
        try
        {
            scm = createScmClient();
            // Temporarily pass the id string through so that the p4 implementation can work with it.
            executionContext.addString("scm.bootstrap.id", getId(executionContext));
            scm.update(executionContext, revision.getRevision(), this);
            return scm;
        }
        catch (ScmException e)
        {
            ScmClientUtils.close(scm);
            LOG.severe(e);
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }
}
