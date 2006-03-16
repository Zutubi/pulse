package com.cinnamonbob.web.admin.user;

import com.cinnamonbob.web.user.UserActionSupport;
import com.cinnamonbob.model.User;

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
