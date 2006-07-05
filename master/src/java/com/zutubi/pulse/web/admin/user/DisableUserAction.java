package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.User;
import com.zutubi.pulse.web.user.UserActionSupport;

/**
 * <class-comment/>
 */
public class DisableUserAction extends UserActionSupport
{
    private int startPage = 0;

    public int getStartPage()
    {
        return startPage;
    }

    public void validate()
    {
        if (getUser() == null)
        {
            addUnknownUserActionError();
        }
    }

    public String execute()
    {
        User user = getUser();
        if (user != null)
        {
            user.setEnabled(false);
            getUserManager().save(user);
            startPage = getUserStartPage(user);
        }
        return SUCCESS;
    }
}
