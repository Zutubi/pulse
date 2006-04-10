package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.web.user.UserActionSupport;
import com.zutubi.pulse.model.User;

/**
 * <class-comment/>
 */
public class DisableUserAction extends UserActionSupport
{
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
        }
        return SUCCESS;
    }
}
