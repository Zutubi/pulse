package com.cinnamonbob.web.admin.user;

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

    public String doInput()
    {
        user = getUserManager().getUser(id);
        return INPUT;
    }

    public String execute()
    {
        User persistentUser = getUserManager().getUser(getId());
        persistentUser.setName(getUser().getName());
        getUserManager().save(persistentUser);
        return SUCCESS;
    }
}
