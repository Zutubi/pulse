package com.zutubi.pulse.web;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.model.UserManager;
import com.zutubi.pulse.prototype.config.user.UserSettingsConfiguration;
import com.zutubi.pulse.security.AcegiUtils;

/**
 * An action to change the users default page to their dashboard once they
 * tire of the welcome page.
 */
public class DismissWelcomeAction extends ActionSupport
{
    private UserManager userManager;
    private ConfigurationProvider configurationProvider;

    public String execute() throws Exception
    {
        String login = AcegiUtils.getLoggedInUser();
        User user = userManager.getUser(login);
        UserSettingsConfiguration settings = configurationProvider.deepClone(user.getPreferences().getSettings());
        settings.setDefaultAction(DefaultAction.DASHBOARD_ACTION);
        configurationProvider.save(settings.getConfigurationPath(), settings);
        return SUCCESS;
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
