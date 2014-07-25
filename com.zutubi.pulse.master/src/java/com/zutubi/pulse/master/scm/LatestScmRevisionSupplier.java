package com.zutubi.pulse.master.scm;

import com.google.common.base.Supplier;
import com.zutubi.pulse.core.engine.api.BuildException;
import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.master.model.Project;
import com.zutubi.util.time.TimeStamps;

import java.util.Locale;

import static com.zutubi.pulse.master.scm.ScmClientUtils.withScmClient;

/**
 * A supplier of revisions that gets the latest revision from a project's SCM.
 */
public class LatestScmRevisionSupplier implements Supplier<Revision>
{
    private Project project;
    private ScmManager scmManager;

    public LatestScmRevisionSupplier(Project project, ScmManager scmManager)
    {
        this.project = project;
        this.scmManager = scmManager;
    }

    public Revision get()
    {
        try
        {
            return withScmClient(project.getConfig(), project.getState(), scmManager, new ScmClientUtils.ScmContextualAction<Revision>()
            {
                public Revision process(ScmClient client, ScmContext context) throws ScmException
                {
                    boolean supportsRevisions = client.getCapabilities(context).contains(ScmCapability.REVISIONS);
                    return supportsRevisions ? client.getLatestRevision(context) : new Revision(TimeStamps.getPrettyDate(System.currentTimeMillis(), Locale.getDefault()));
                }
            });
        }
        catch (ScmException e)
        {
            throw new BuildException("Unable to retrieve latest revision for project '" + project.getName() + "': " + e.getMessage(), e);
        }
    }
}
