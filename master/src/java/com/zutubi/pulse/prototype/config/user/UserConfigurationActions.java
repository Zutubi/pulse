package com.zutubi.pulse.prototype.config.user;

import com.zutubi.pulse.model.UserManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Actions available for users.
 */
public class UserConfigurationActions
{
    private UserManager userManager;

    public List<String> getActions(UserConfiguration user)
    {
        if(user.isAuthenticatedViaLdap())
        {
            return Collections.EMPTY_LIST;
        }
        else
        {
            return Arrays.asList("setPassword");
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
