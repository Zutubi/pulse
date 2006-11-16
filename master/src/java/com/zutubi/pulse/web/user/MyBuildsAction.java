package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.security.AcegiUtils;
import com.zutubi.pulse.web.ActionSupport;

import java.util.List;

/**
 * Action to view the user's personal build results.
 */
public class MyBuildsAction extends ActionSupport
{
    private User user;
    private List<BuildResult> myBuilds;

    private BuildManager buildManager;
    private UserManager userManager;

    public User getUser()
    {
        return user;
    }

    public List<BuildResult> getMyBuilds()
    {
        return myBuilds;
    }

    public BuildColumns getColumns()
    {
        return new BuildColumns(user.getMyBuildsColumns(), projectManager);
    }

    public String execute() throws Exception
    {
        String login = AcegiUtils.getLoggedInUser();
        if (login == null)
        {
            return "guest";
        }
        user = userManager.getUser(login);
        if (user == null)
        {
            return ERROR;
        }

        myBuilds = buildManager.getPersonalBuilds(user);
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
