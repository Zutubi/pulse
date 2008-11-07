package com.zutubi.pulse.master.tove.config.project.changeviewer;

import com.zutubi.pulse.core.scm.api.*;
import com.zutubi.pulse.core.scm.config.api.ScmConfiguration;

/**
 * A default implementation of {@link com.zutubi.pulse.master.tove.config.project.changeviewer.ChangeContext
 */
public class ChangeContextImpl implements ChangeContext
{
    private Changelist changelist;
    private ScmConfiguration scmConfiguration;
    private ScmClient scmClient;
    private ScmContext scmContext;

    public ChangeContextImpl(Changelist changelist, ScmConfiguration scmConfiguration, ScmClient scmClient, ScmContext scmContext)
    {
        this.changelist = changelist;
        this.scmConfiguration = scmConfiguration;
        this.scmClient = scmClient;
        this.scmContext = scmContext;
    }

    public Changelist getChangelist()
    {
        return changelist;
    }

    public ScmConfiguration getScmConfiguration()
    {
        return scmConfiguration;
    }

    public ScmClient getScmClient()
    {
        return scmClient;
    }

    public ScmContext getScmContext()
    {
        return scmContext;
    }

    public Revision getPreviousChangelistRevision() throws ScmException
    {
        return scmClient.getPreviousRevision(scmContext, changelist.getRevision(), false);
    }

    public Revision getPreviousFileRevision(FileChange fileChange) throws ScmException
    {
        return scmClient.getPreviousRevision(scmContext, fileChange.getRevision(), true);
    }
}
