package com.cinnamonbob.web.user;

import com.opensymphony.xwork.ActionSupport;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.model.User;
import com.cinnamonbob.web.BaseActionSupport;

/**
 * 
 *
 */
public class LoadUserAction extends BaseActionSupport
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
