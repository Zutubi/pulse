package com.zutubi.pulse.web.user;

import com.zutubi.util.TextUtils;
import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.prototype.config.user.UserSettingsConfiguration;

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

        if(!TextUtils.stringSet(suffix))
        {
            return ERROR;
        }

        UserSettingsConfiguration settings = configurationProvider.deepClone(user.getPreferences().getSettings());
        if(suffix.equals("my"))
        {
            settings.setMyBuildsColumns(columns);
        }
        else if(suffix.equals("my.projects"))
        {
            settings.setMyProjectsColumns(columns);
        }
        else if(suffix.equals("all.projects"))
        {
            settings.setAllProjectsColumns(columns);
        }
        else if(suffix.equals("project.summary"))
        {
            settings.setProjectSummaryColumns(columns);
        }
        else if(suffix.equals("project.recent"))
        {
            settings.setProjectRecentColumns(columns);
        }
        else if(suffix.equals("project.history"))
        {
            settings.setProjectHistoryColumns(columns);
        }

        configurationProvider.save(settings);
        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
