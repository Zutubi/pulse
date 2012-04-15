package com.zutubi.pulse.servercore;

import com.zutubi.pulse.core.BuildRevision;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.engine.api.ExecutionContext;
import com.zutubi.pulse.core.scm.api.ScmClient;
import com.zutubi.pulse.core.scm.api.ScmException;
import com.zutubi.util.io.FileSystemUtils;
import com.zutubi.util.io.IOUtils;
import com.zutubi.util.logging.Logger;

/**
 * Bootstrapper which runs an incremental update on a working copy using the
 * project's SCM.
 */
public class UpdateBootstrapper extends ScmBootstrapper
{
    private static final Logger LOG = Logger.getLogger(UpdateBootstrapper.class);

    public UpdateBootstrapper(String project, BuildRevision revision)
    {
        super(project, revision);
    }

    ScmClient doBootstrap(ExecutionContext executionContext)
    {
        writeFeedback("Updating " + FileSystemUtils.getNormalisedAbsolutePath(executionContext.getWorkingDir()));
        ScmClient scm = null;
        try
        {
            scm = createScmClient(executionContext);
            scm.update(executionContext, revision.getRevision(), this);
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
