package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.admin.GlobalConfiguration;
import com.zutubi.pulse.master.tove.config.user.BrowseViewConfiguration;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * Action for the browse view.  Most of the work is done by {@link BrowseDataAction}
 * and client side JavaScript.  This action just takes care of proper direction
 * of anonymous users.
 */
public class BrowseAction extends ActionSupport
{
    private int columnCount;
    private ConfigurationProvider configurationProvider;

    public int getColumnCount()
    {
        return columnCount;
    }

    @Override
    public String execute() throws Exception
    {
        User user = getLoggedInUser();
        if (configurationProvider.get(GlobalConfiguration.class).isAnonymousAccessEnabled() || user != null)
        {
            BrowseViewConfiguration browseConfig = user == null ? new BrowseViewConfiguration() : user.getPreferences().getBrowseView();
            columnCount = browseConfig.getColumns().size();
            return SUCCESS;
        }
        else
        {
            return "guest";
        }
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
