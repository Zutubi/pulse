package com.cinnamonbob.web;

import com.cinnamonbob.model.User;
import com.cinnamonbob.model.UserManager;

/**
 * <class-comment/>
 */
public class DefaultAction extends ActionSupport
{
    public static final String WELCOME_ACTION = "welcome";
    public static final String DASHBOARD_ACTION = "dashboard";

    private UserManager userManager;

    public String execute()
    {
        if (userManager.getUserCount() > 0)
        {
            return ((User) getPrinciple()).getDefaultAction();
        }
        return "setupAdmin";
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
