package com.cinnamonbob.web.user;

import com.cinnamonbob.model.User;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.security.AcegiUtils;

/**
 * <class-comment/>
 */
public class PreferencesAction extends UserActionSupport
{
    private UserManager userManager;

    private User user;

    public User getUser()
    {
        return user;
    }

    public String execute()
    {
        user = (User) AcegiUtils.getLoggedInUser();
        if (user == null)
        {
            return ERROR;
        }

        // load the user from the db.
        user = userManager.getUser(user.getLogin());

        return SUCCESS;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
