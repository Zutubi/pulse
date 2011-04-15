package com.zutubi.pulse.master.xwork.actions.server;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.ajax.SimpleResult;

/**
 * Ajax action to cancel a running build.
 */
public class CancelBuildAction extends ActionSupport
{
    private static final Messages I18N = Messages.getInstance(CancelBuildAction.class);
    
    private long buildId;
    private SimpleResult result;
    private BuildManager buildManager;

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public SimpleResult getResult()
    {
        return result;
    }

    public String execute() throws Exception
    {
        BuildResult build = buildManager.getBuildResult(buildId);
        if (build == null)
        {
            throw new IllegalArgumentException("Unknown build '" + buildId + "'");
        }

        String user = getPrinciple();
        buildManager.terminateBuild(build, user == null ? null : "requested by '" + user + "'");
        pauseForDramaticEffect();
        result = new SimpleResult(true, I18N.format("termination.requested"));
        return SUCCESS;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
