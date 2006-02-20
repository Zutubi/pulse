package com.cinnamonbob.web.user;

import com.cinnamonbob.user.User;

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

    public String doInput()
    {
        user = getUserManager().getUser(id);
        return INPUT;
    }

    public String execute()
    {
        User persistentUser = getUserManager().getUser(getId());
        persistentUser.setName(getUser().getName());
        return SUCCESS;
    }
}
