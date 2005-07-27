package com.cinnamonbob.web.user;

import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.model.User;
import com.cinnamonbob.web.ActionSupport;

/**
 * 
 *
 */
public class LoadUserAction extends ActionSupport
{
    private long id;

    private UserManager userManager;

    private User user;

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

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

    public String execute()
    {
        user = userManager.getUser(id);
        return SUCCESS;
    }
}
