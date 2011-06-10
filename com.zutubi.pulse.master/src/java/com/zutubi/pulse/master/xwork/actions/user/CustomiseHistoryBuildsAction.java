package com.zutubi.pulse.master.xwork.actions.user;

import com.zutubi.pulse.master.xwork.actions.ajax.HistoryContext;
import com.zutubi.tove.config.ConfigurationProvider;

/**
 * Action to save a user's choice of builds per page on history views.
 */
public class CustomiseHistoryBuildsAction extends UserActionSupport
{
    private int buildsPerPage;
    private ConfigurationProvider configurationProvider;

    public void setBuildsPerPage(int buildsPerPage)
    {
        this.buildsPerPage = buildsPerPage;
    }

    @SuppressWarnings({"unchecked"})
    public String execute() throws Exception
    {
        if (buildsPerPage <= 0)
        {
            return ERROR;
        }

        HistoryContext.setBuildsPerPage(getUser(), buildsPerPage, configurationProvider);
        return SUCCESS;
    }

    public void setConfigurationProvider(ConfigurationProvider configurationProvider)
    {
        this.configurationProvider = configurationProvider;
    }
}
