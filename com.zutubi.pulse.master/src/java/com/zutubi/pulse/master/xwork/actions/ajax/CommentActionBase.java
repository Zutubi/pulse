package com.zutubi.pulse.master.xwork.actions.ajax;

import com.zutubi.i18n.Messages;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * Helper base class for actions that manipulate comments.
 */
public abstract class CommentActionBase extends ActionSupport
{
    private static final Messages I18N = Messages.getInstance(CommentActionBase.class);

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

    @Override
    public String execute() throws Exception
    {
        try
        {
            BuildResult build = buildManager.getBuildResult(buildId);
            if (build == null)
            {
                throw new IllegalArgumentException(I18N.format("uknown.build", buildId));
            }

            User user = getLoggedInUser();
            if (user == null)
            {
                throw new IllegalStateException(I18N.format("not.logged.in"));
            }

            updateBuild(build, user);
            buildManager.save(build);
            result = new SimpleResult(true, "");
        }
        catch (Exception e)
        {
            result = new SimpleResult(false, e.getMessage());
        }

        return SUCCESS;
    }

    protected abstract void updateBuild(BuildResult build, User user);

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }
}
