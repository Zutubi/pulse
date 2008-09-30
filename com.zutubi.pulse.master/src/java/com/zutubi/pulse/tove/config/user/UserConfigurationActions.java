package com.zutubi.pulse.tove.config.user;

import com.zutubi.pulse.model.UserManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Actions available for users.
 */
public class UserConfigurationActions
{
    public static final String ACTION_SET_PASSWORD = "setPassword";
    
    private UserManager userManager;

    public List<String> getActions(UserConfiguration user)
    {
        if(user.isAuthenticatedViaLdap())
        {
            return Collections.emptyList();
        }
        else
        {
            return Arrays.asList(ACTION_SET_PASSWORD);
        }
    }

    public void doSetPassword(UserConfiguration user, SetPasswordConfiguration password)
    {
        userManager.setPassword(user, password.getPassword());
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}
