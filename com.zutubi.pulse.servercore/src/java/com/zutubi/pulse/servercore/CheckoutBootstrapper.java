package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

/**
 * A bootstrapper that runs a clean checkout from the project's SCM.
 */
public class CheckoutBootstrapper extends ScmBootstrapper
{
    private static final Logger LOG = Logger.getLogger(CheckoutBootstrapper.class);

    public CheckoutBootstrapper(String project, BuildRevision revision)
    {
        super(project, revision);
    }

    public ScmClient doBootstrap(ExecutionContext executionContext)
    {
        ScmClient scm = null;
        try
        {
            scm = createScmClient(executionContext);
            scm.checkout(executionContext, revision.getRevision(), this);
            return scm;
        }
        catch (ScmException e)
        {
            IOUtils.close(scm);
            LOG.severe(e);
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }
}
