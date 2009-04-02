package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.model.BuildColumns;
import com.zutubi.pulse.master.model.BuildManager;
import com.zutubi.pulse.master.model.BuildResult;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.security.AcegiUtils;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

import java.util.List;

/**
 * Action to view the user's personal build results.
 */
public class MyBuildsAction extends ActionSupport
{
    private User user;
    private List<BuildResult> myBuilds;

    private BuildManager buildManager;
    private BuildColumns columns;

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
        if (columns == null)
        {
            columns = new BuildColumns(user.getPreferences().getMyBuildsColumns(), accessManager);
        }
        return columns;
    }

    public String execute() throws Exception
    {
        String login = AcegiUtils.getLoggedInUsername();
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
}
