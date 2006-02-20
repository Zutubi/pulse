package com.cinnamonbob.web.user;

import com.cinnamonbob.model.User;

/**
 *
 *
 */
public class EditUserAction extends UserActionSupport
{
    private long id;

    private User user = new User();

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public User getUser()
    {
        return user;
    }

    public String doDefault()
    {
        user = getUserManager().getUser(id);
        return SUCCESS;
    }

    public String execute()
    {
        User persistentUser = getUserManager().getUser(getId());
        persistentUser.setName(getUser().getName());
        return SUCCESS;
    }
}
