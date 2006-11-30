package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.ProjectGroup;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.security.AcegiUtils;

/**
 * Action allowing a user to hide a chosen project group from their dashboard.
 */
public class HideDashboardGroupAction extends UserActionSupport
{
    private long id;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String execute() throws Exception
    {
        String login = AcegiUtils.getLoggedInUser();
        if (login == null)
        {
            return ERROR;
        }

        setUserLogin(login);

        User user = getUser();

        ProjectGroup g = projectManager.getProjectGroup(id);
        if(g != null)
        {
            user.getShownGroups().remove(g);
        }

        getUserManager().save(user);
        return SUCCESS;
    }
}
