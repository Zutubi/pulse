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

    public SCMServer bootstrap(File workDir)
    {
        SCMServer server = null;
        try
        {
            String id = null;
            if(persist)
            {
                id = getId();
            }

            server = scm.createServer();
            server.checkout(id, workDir, revision.getRevision(), this);
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
