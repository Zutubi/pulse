package com.cinnamonbob.web.admin.user;

import com.cinnamonbob.web.user.UserActionSupport;
import com.cinnamonbob.model.User;

/**
 * <class-comment/>
 */
public class DisableUserAction extends UserActionSupport
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

    public String execute()
    {
        User user = getUserManager().getUser(id);
        if (user != null)
        {
            user.setEnabled(false);
            getUserManager().save(user);
        }
        return SUCCESS;
    }
}
