package com.zutubi.pulse.web.admin.user;

import com.zutubi.pulse.security.AcegiUtils;

/**
 * <class comment/>
 */
public class SignupUserAction extends CreateUserAction
{
    public String execute() throws Exception
    {
        if (!getConfigurationManager().getAppConfig().getAnonymousSignupEnabled())
        {
            return "disabled";
        }

        getUserManager().addUser(newUser, false, false);

        // log the new user in.
        AcegiUtils.loginAs(newUser);

        return SUCCESS;
    }
}
