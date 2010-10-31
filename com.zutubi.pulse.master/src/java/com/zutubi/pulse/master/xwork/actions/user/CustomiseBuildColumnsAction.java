package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.tove.config.ConfigurationProvider;
import com.zutubi.util.StringUtils;

/**
 * Action to save a user's choice of columns in a build summary table.
 */
public class CustomiseBuildColumnsAction extends UserActionSupport
{
    private String tableId;
    private String columns;
    private ConfigurationProvider configurationProvider;

    public void setTableId(String tableId)
    {
        this.tableId = tableId;
    }

    public void setColumns(String columns)
    {
        this.columns = columns;
    }

    public String execute() throws Exception
    {
        User user = getUser();
        if (user == null)
        {
            return ERROR;
        }

        if (!StringUtils.stringSet(tableId))
        {
            return ERROR;
        }

        UserPreferencesConfiguration preferences = configurationProvider.deepClone(user.getPreferences());
        if (tableId.equals("my-builds-builds"))
        {
            preferences.setMyBuildsColumns(columns);
        }
        else if (tableId.equals("project-recent"))
        {
            preferences.setProjectRecentColumns(columns);
        }
        else if (tableId.equals("project-history"))
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
