package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmContext;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.util.logging.Logger;

/**
 * <class-comment/>
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

    public ScmClient bootstrap(ScmContext context)
    {
        try
        {
            String id = null;
            if(persist)
            {
                id = getId();
            }
            context.setId(id);
            context.setRevision(revision.getRevision());
            ScmClient scm = createScmClient();
            scm.checkout(context, this);
            return scm;
        }
        catch (ScmException e)
        {
            LOG.severe(e);
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }
}
