package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;
import com.zutubi.pulse.core.scm.ScmClientUtils;
import com.zutubi.util.logging.Logger;

/**
 * A bootstrapper that runs a clean checkout from the project's SCM.
 */
public class CheckoutBootstrapper extends ScmBootstrapper
{
    private static final Logger LOG = Logger.getLogger(CheckoutBootstrapper.class);

    private boolean persist;

    public CheckoutBootstrapper(String project, ScmConfiguration scmConfig, BuildRevision revision, boolean persist)
    {
        super(project, scmConfig, revision);
        this.persist = persist;
    }

    public ScmClient doBootstrap(ExecutionContext executionContext)
    {
        ScmClient scm = null;
        try
        {
            String id = null;
            if(persist)
            {
                id = getId(executionContext);
            }
            executionContext.addString("scm.bootstrap.id", id);
            scm = createScmClient(executionContext);
            scm.checkout(executionContext, revision.getRevision(), this);
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
