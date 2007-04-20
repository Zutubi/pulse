package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.model.Scm;
import com.zutubi.pulse.scm.SCMException;
import com.zutubi.pulse.scm.SCMClient;
import com.zutubi.util.logging.Logger;

import java.io.File;

/**
 * <class-comment/>
 */
public class CheckoutBootstrapper extends ScmBootstrapper
{
    private static final Logger LOG = Logger.getLogger(CheckoutBootstrapper.class);

    private boolean persist;

    public CheckoutBootstrapper(String project, String spec, Scm scm, BuildRevision revision, boolean persist)
    {
        super(project, spec, scm, revision);
        this.persist = persist;
    }

    public SCMClient bootstrap(File workDir)
    {
        try
        {
            String id = null;
            if(persist)
            {
                id = getId();
            }

            SCMClient client = scm.createServer();
            client.checkout(id, workDir, revision.getRevision(), this);
            return client;
        }
        catch (SCMException e)
        {
            LOG.severe(e);
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }
}
