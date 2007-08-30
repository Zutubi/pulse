package com.zutubi.pulse;

import com.zutubi.pulse.core.BuildException;
import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.scm.ScmException;
import com.zutubi.pulse.core.scm.ScmClient;
import com.zutubi.pulse.core.scm.ScmContext;
import com.zutubi.pulse.core.scm.config.ScmConfiguration;
import com.zutubi.util.logging.Logger;

/**
 */
public class UpdateBootstrapper extends ScmBootstrapper
{
    private static final Logger LOG = Logger.getLogger(UpdateBootstrapper.class);

    public UpdateBootstrapper(String project, ScmConfiguration scmConfig, BuildRevision revision)
    {
        super(project, scmConfig, revision);
    }

    ScmClient bootstrap(ScmContext context)
    {
        try
        {
            context.setId(getId());
            context.setRevision(revision.getRevision());
            ScmClient scm = createScmClient();
            scm.update(context, this);
            return scm;
        }
        catch (ScmException e)
        {
            LOG.severe(e);
            throw new BuildException("Error checking out from SCM: " + e.getMessage(), e);
        }
    }
}
