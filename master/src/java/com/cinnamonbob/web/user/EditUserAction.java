package com.cinnamonbob.web.user;

import com.cinnamonbob.model.User;

/**
 * <class-comment/>
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

    public String doInput() throws Exception
    {
        user = getUserManager().getUser(id);
        return super.doInput();
    }

    public String execute()
    {
        User persistentUser = getUserManager().getUser(getId());
        persistentUser.setName(user.getName());
        getUserManager().save(persistentUser);
        return SUCCESS;
    }
}
