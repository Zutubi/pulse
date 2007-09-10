package com.zutubi.pulse.web.server;

import com.zutubi.pulse.FatController;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.web.ActionSupport;
import org.acegisecurity.AccessDeniedException;

/**
 */
public class CancelBuildAction extends ActionSupport
{
    private long buildId;
    private FatController fatController;
    private BuildManager buildManager;
    private ProjectManager projectManager;
    private UserManager userManager;

    public void setBuildId(long buildId)
    {
        this.buildId = buildId;
    }

    public String execute() throws Exception
    {
        BuildResult build = buildManager.getBuildResult(buildId);
        Object principle = getPrinciple();
        if (build != null && principle != null && principle instanceof String)
        {
            User user = userManager.getUser((String) principle);
            if (!buildManager.canCancel(build, user))
            {
                throw new AccessDeniedException("Insufficient authority to cancel build");
            }

            fatController.terminateBuild(buildId, false);
            Thread.sleep(500);
        }
        
        return SUCCESS;
    }

    public void setFatController(FatController fatController)
    {
        this.fatController = fatController;
    }

    public void setBuildManager(BuildManager buildManager)
    {
        this.buildManager = buildManager;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
