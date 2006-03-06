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

    public String doInput() throws Exception
    {
        String login = AcegiUtils.getLoggedInUser();
        if (login == null)
        {
            return ERROR;
        }

        // load the user from the db.
        user = userManager.getUser(login);
        if (user == null)
        {
            return ERROR;
        }
        return super.doInput();
    }

    public String execute() throws Exception
    {
        String result = doInput();
        if (result.equals(INPUT))
        {
            return SUCCESS;
        }
        return ERROR;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
