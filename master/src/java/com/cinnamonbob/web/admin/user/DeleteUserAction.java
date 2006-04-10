package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.model.User;
import com.zutubi.pulse.web.user.UserActionSupport;
import com.zutubi.pulse.security.AcegiUtils;

/**
 * <class-comment/>
 */
public class DeleteUserAction extends UserActionSupport
{
    public void validate()
    {
        if (getUser() == null)
        {
            addUnknownUserActionError();
        }

        // check if the user is currently logged in.
        String loggedInUser = AcegiUtils.getLoggedInUser();
        if (getUser().getLogin().equals(loggedInUser))
        {
            addActionError(getText("user.delete.self"));
        }
    }

    public String execute()
    {
        User user = getUser();
        getUserManager().delete(user);
        return SUCCESS;
    }
}
