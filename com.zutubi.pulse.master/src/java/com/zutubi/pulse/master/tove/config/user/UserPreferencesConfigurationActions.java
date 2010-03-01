package com.zutubi.pulse.master.tove.config.user;

import com.zutubi.pulse.master.model.UserManager;
import com.zutubi.tove.config.ConfigurationProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Actions available for user preferences.
 */
public class UserPreferencesConfigurationActions
{
    public static final String ACTION_CHANGE_PASSWORD = "changePassword";
    
    private ConfigurationProvider configurationProvider;
    private UserManager userManager;

    public List<String> getActions(UserPreferencesConfiguration userPreferences)
    {
        UserConfiguration user = configurationProvider.getAncestorOfType(userPreferences, UserConfiguration.class);
        if (user.isAuthenticatedViaLdap())
        {
            return Collections.emptyList();
        }
        else
        {
            return Arrays.asList(ACTION_CHANGE_PASSWORD);
        }
    }

    public void doChangePassword(UserPreferencesConfiguration userPreferences, ChangePasswordConfiguration password)
    {
        UserConfiguration user = configurationProvider.getAncestorOfType(userPreferences, UserConfiguration.class);
        userManager.setPassword(user, password.getNewPassword());
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }
}