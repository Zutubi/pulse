package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.scm.ScmException;
import com.zutubi.pulse.servercore.config.ScmConfiguration;
import com.zutubi.pulse.servercore.scm.ScmClient;
import com.zutubi.util.logging.Logger;

import java.io.File;

/**
 * <class-comment/>
 */
public class CheckoutBootstrapper extends ScmBootstrapper
{
    private static final Logger LOG = Logger.getLogger(CheckoutBootstrapper.class);

    private boolean persist;

    public CheckoutBootstrapper(String project, ScmConfiguration scm, BuildRevision revision, boolean persist)
    {
        super(project, scm, revision);
        this.persist = persist;
    }

    public ScmClient bootstrap(File workDir)
    {
        try
        {
            String id = null;
            if(persist)
            {
                id = getId();
            }

            ScmClient client = scm.createClient();
            client.checkout(id, workDir, revision.getRevision(), this);
            return client;
        }
        catch (ScmException e)
        {
            LOG.severe(e);
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }
}
