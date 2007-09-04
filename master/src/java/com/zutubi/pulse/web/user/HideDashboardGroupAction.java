package com.zutubi.pulse.web.user;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.prototype.config.user.DashboardConfiguration;
import com.zutubi.pulse.security.AcegiUtils;

/**
 * Action allowing a user to hide a chosen project group from their dashboard.
 */
public class HideDashboardGroupAction extends UserActionSupport
{
    private String groupName;
    private ConfigurationProvider configurationProvider;

    public String getGroupName()
    {
        return groupName;
    }

    public void setGroupName(String groupName)
    {
        this.groupName = groupName;
    }

    public String execute() throws Exception
    {
        String login = AcegiUtils.getLoggedInUser();
        if (login == null)
        {
            return ERROR;
        }

        setUserLogin(login);

        User user = getUser();

        DashboardConfiguration configuration = user.getPreferences().getDashboard();
        configuration.getShownGroups().remove(groupName);
        configurationProvider.save(configuration);
        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
