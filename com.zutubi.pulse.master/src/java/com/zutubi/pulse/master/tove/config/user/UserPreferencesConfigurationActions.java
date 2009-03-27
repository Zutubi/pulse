package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.pulse.master.security.AcegiUser;
import com.zutubi.pulse.master.security.AcegiUtils;
import com.zutubi.tove.config.ConfigurationProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Actions available for users from their own preferences page.
 */
public class UserPreferencesConfigurationActions
{
    public static final String ACTION_SET_PASSWORD = "setPassword";

    private ConfigurationProvider configurationProvider;
    private UserManager userManager;

    public List<String> getActions(UserPreferencesConfiguration preferences)
    {
        UserConfiguration user = configurationProvider.getAncestorOfType(preferences, UserConfiguration.class);
        if(user.isAuthenticatedViaLdap())
        {
            return Collections.emptyList();
        }
        else
        {
            return Arrays.asList(ACTION_SET_PASSWORD);
        }
    }

    public void doSetPassword(UserPreferencesConfiguration preferences, SetOwnPasswordConfiguration password)
    {
        UserConfiguration user = configurationProvider.getAncestorOfType(preferences, UserConfiguration.class);
        userManager.setPassword(user, password.getPassword());

        // Refresh the logged in user information.
        AcegiUser principle = userManager.getPrinciple(user);
        AcegiUtils.loginAs(principle);
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}