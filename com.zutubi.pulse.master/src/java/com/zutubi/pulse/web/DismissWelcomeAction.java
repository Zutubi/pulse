package com.zutubi.pulse.web;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.security.AcegiUtils;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * An action to change the users default page to their dashboard once they
 * tire of the welcome page.
 */
public class DismissWelcomeAction extends ActionSupport
{
    private ConfigurationProvider configurationProvider;

    public String execute() throws Exception
    {
        String login = AcegiUtils.getLoggedInUsername();
        User user = userManager.getUser(login);
        UserPreferencesConfiguration preferences = configurationProvider.deepClone(user.getPreferences());
        preferences.setDefaultAction(DefaultAction.DASHBOARD_ACTION);
        configurationProvider.save(preferences);
        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
