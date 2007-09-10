package com.zutubi.pulse.web.server;

import com.zutubi.pulse.FatController;
import com.zutubi.pulse.events.build.AbstractBuildRequestEvent;
import com.zutubi.pulse.model.BuildManager;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.web.ActionSupport;
import org.acegisecurity.AccessDeniedException;

/**
 * <class comment/>
 */
public class CancelQueuedBuildAction extends ActionSupport
{
    /**
     * The id of the queued build request event to be cancelled.
     */
    private long id;

    private FatController fatController;
    private BuildManager buildManager;
    private UserManager userManager;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public void setFatController(FatController fatController)
    {
        this.fatController = fatController;
    }

    public String execute() throws Exception
    {
        // if the queued build request does not exist, then the build has started. It will need to be cancelled
        // separately.
        AbstractBuildRequestEvent event = fatController.findQueuedBuild(id);
        Object principle = getPrinciple();
        if (event != null && principle != null && principle instanceof String)
        {
            User user = userManager.getUser((String) principle);
            if (!buildManager.canCancel(event, user))
            {
                throw new AccessDeniedException("Insufficient authority to cancel build");
            }
            fatController.cancelQueuedBuild(id);
        }

        return SUCCESS;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
