package com.cinnamonbob.web.admin.user;

import com.cinnamonbob.model.User;

import java.util.Arrays;

/**
 * <class-comment/>
 */
public class DeleteUserAction extends UserActionSupport
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

    public void validate()
    {
        if (getUserManager().getUser(id) == null)
        {
            addFieldError("id", getText("user.id.unknown", Arrays.asList(id)));
        }
    }

    public String execute()
    {
        User user = getUserManager().getUser(id);
        if (user != null)
        {
            getUserManager().delete(user);
        }
        return SUCCESS;
    }
}
