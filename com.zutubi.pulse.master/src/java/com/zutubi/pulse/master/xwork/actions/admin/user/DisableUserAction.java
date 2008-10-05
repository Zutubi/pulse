package com.zutubi.pulse.master.xwork.actions.admin.user;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.xwork.actions.user.UserActionSupport;

/**
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
            userManager.save(user);
        }
        return SUCCESS;
    }
}
