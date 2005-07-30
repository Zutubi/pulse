package com.cinnamonbob.web.user;

import com.cinnamonbob.web.ActionSupport;
import com.cinnamonbob.model.UserManager;

/**
 *
 * 
 */
public class UserActionSupport extends ActionSupport
{
    private UserManager userManager;

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public UserManager getUserManager()
    {
        return userManager;
    }
}
