package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.StringUtils;

/**
 */
public class CustomiseBuildColumnsAction extends UserActionSupport
{
    private String suffix;
    private String columns;
    private ConfigurationProvider configurationProvider;

    public void setSuffix(String suffix)
    {
        this.suffix = suffix;
    }

    public void setColumns(String columns)
    {
        this.columns = columns;
    }

    public String execute() throws Exception
    {
        User user = getUser();
        if(user == null)
        {
            return ERROR;
        }

        if(!StringUtils.stringSet(suffix))
        {
            return ERROR;
        }

        UserPreferencesConfiguration preferences = configurationProvider.deepClone(user.getPreferences());
        if(suffix.equals("my"))
        {
            preferences.setMyBuildsColumns(columns);
        }
        else if(suffix.equals("my.projects"))
        {
            preferences.setMyProjectsColumns(columns);
        }
        else if(suffix.equals("project.summary"))
        {
            preferences.setProjectSummaryColumns(columns);
        }
        else if(suffix.equals("project.recent"))
        {
            preferences.setProjectRecentColumns(columns);
        }
        else if(suffix.equals("project.history"))
        {
            preferences.setProjectHistoryColumns(columns);
        }

        configurationProvider.save(preferences);
        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
