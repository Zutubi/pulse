package com.cinnamonbob.web;

import com.cinnamonbob.model.UserManager;

/**
 * <class-comment/>
 */
public class DefaultAction extends ActionSupport
{
    private UserManager userManager;

    public String execute()
    {
        if (userManager.getUserCount() > 0)
        {
            return SUCCESS;
        }
        return "setupAdmin";
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
