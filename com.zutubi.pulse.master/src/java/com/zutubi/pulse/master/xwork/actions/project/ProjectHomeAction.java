package com.zutubi.pulse.master.xwork.actions.project;

import com.zutubi.pulse.master.model.User;
import com.zutubi.pulse.master.tove.config.user.UserPreferencesConfiguration;

/**
 * Action to display project home page.  Just ensures the project exists, and
 * loads customisations.  The real work is done with an AJAX request to
 * {@link com.zutubi.pulse.master.xwork.actions.project.ProjectHomeDataAction}.
 */
public class ProjectHomeAction extends ProjectActionBase
{
    private String projectRecentColumns = UserPreferencesConfiguration.defaultShortProjectColumns();

    public String getProjectRecentColumns()
    {
        return projectRecentColumns;
    }

    public String execute()
    {
        getRequiredProject();
        
        User user = getLoggedInUser();
        if (user != null)
        {
            projectRecentColumns = user.getConfig().getPreferences().getProjectRecentColumns();
        }
        
        return SUCCESS;
    }
}
