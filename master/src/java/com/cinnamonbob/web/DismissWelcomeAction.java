package com.cinnamonbob.web;

import com.cinnamonbob.model.User;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.security.AcegiUtils;

/**
 * An action to change the users default page to their dashboard once they
 * tire of the welcome page.
 */
public class DismissWelcomeAction extends ActionSupport
{
    private UserManager userManager;

    public String execute() throws Exception
    {
        String login = AcegiUtils.getLoggedInUser();
        User user = userManager.getUser(login);
        user.setDefaultAction(DefaultAction.DASHBOARD_ACTION);
        userManager.save(user);
        return SUCCESS;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
