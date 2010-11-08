package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.model.ProjectGroup;
import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.security.SecurityUtils;
import com.zutubi.pulse.master.tove.config.user.DashboardConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.CollectionUtils;
import com.zutubi.util.Mapping;

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
        String login = SecurityUtils.getLoggedInUsername();
        if (login == null)
        {
            return ERROR;
        }

        setUserLogin(login);

        User user = getUser();

        DashboardConfiguration configuration = configurationProvider.deepClone(user.getPreferences().getDashboard());
        if(configuration.isShowAllGroups())
        {
            configuration.setShowAllGroups(false);
            configuration.getShownGroups().addAll(CollectionUtils.map(projectManager.getAllProjectGroups(), new Mapping<ProjectGroup, String>()
            {
                public String map(ProjectGroup projectGroup)
                {
                    return projectGroup.getName();
                }
            }));
        }
        
        configuration.getShownGroups().remove(groupName);
        configurationProvider.save(configuration);
        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
