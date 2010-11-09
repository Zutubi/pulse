package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;

/**
 * Action to view the user's personal build results.
 */
public class MyBuildsAction extends ActionSupport
{
    private User user;
    private String columns;

    public String getColumns()
    {
        if (columns == null)
        {
            columns = user.getPreferences().getMyBuildsColumns();
        }
        return columns;
    }

    /**
     * Note that this is required by the main decorator for toolbar rendering.
     * 
     * @return true to indicate the user is looking at personal builds
     */
    public boolean isPersonalBuild()
    {
        return true;
    }

    public String execute() throws Exception
    {
        String login = SecurityUtils.getLoggedInUsername();
        if (login == null)
        {
            return "guest";
        }
        
        user = userManager.getUser(login);
        if (user == null)
        {
            return ERROR;
        }

        return SUCCESS;
    }
}
