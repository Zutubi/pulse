package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;
import com.zutubi.pulse.master.xwork.actions.ajax.HistoryContext;

/**
 * Action for viewing project history.
 */
public class ProjectHistoryAction extends ProjectActionBase
{
    private int startPage = 0;
    private String stateFilter = "";
    private int buildsPerPage;
    private String columns = UserPreferencesConfiguration.defaultProjectColumns();

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
        getRequiredProject();
        
        User user = getLoggedInUser();
        if (user != null)
        {
            columns = user.getConfig().getPreferences().getProjectHistoryColumns();
        }
        
        buildsPerPage = HistoryContext.getBuildsPerPage(user);
        return SUCCESS;
    }
}
