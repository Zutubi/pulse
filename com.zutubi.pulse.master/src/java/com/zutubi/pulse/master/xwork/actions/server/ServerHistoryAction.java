package com.zutubi.pulse.master.xwork.actions.server;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.pulse.master.xwork.actions.ActionSupport;
import com.zutubi.pulse.master.xwork.actions.ajax.HistoryContext;

/**
 * Action for viewing server-wide build history.
 */
public class ServerHistoryAction extends ActionSupport
{
    private int startPage = 0;
    private String stateFilter = "";
    private int buildsPerPage;
    private String columns = UserPreferencesConfiguration.defaultGlobalColumns();

    public int getStartPage()
    {
        return startPage;
    }

    public void setStartPage(int startPage)
    {
        this.startPage = startPage;
    }

    public String getStateFilter()
    {
        return stateFilter;
    }

    public void setStateFilter(String stateFilter)
    {
        this.stateFilter = stateFilter;
    }

    public int getBuildsPerPage()
    {
        return buildsPerPage;
    }

    public String getColumns()
    {
        return columns;
    }

    public String execute() throws Exception
    {
        User user = getLoggedInUser();
        if (user != null)
        {
            columns = user.getConfig().getPreferences().getServerHistoryColumns();
        }
     
        buildsPerPage = HistoryContext.getBuildsPerPage(user);
        return SUCCESS;
    }
}
