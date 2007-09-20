package com.zutubi.pulse.web.user;

import com.zutubi.prototype.config.ConfigurationProvider;
import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.prototype.config.user.DashboardConfiguration;
import com.zutubi.pulse.security.AcegiUtils;

/**
 * Action allowing a user to hide a chosen project from their dashboard.
 */
public class HideDashboardProjectAction extends UserActionSupport
{
    private long id;
    private ConfigurationProvider connfigurationProvider;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String execute() throws Exception
    {
        String login = AcegiUtils.getLoggedInUsername();
        if (login == null)
        {
            return ERROR;
        }

        setUserLogin(login);

        User user = getUser();
        DashboardConfiguration dashboardConfig = user.getConfig().getPreferences().getDashboard();

        Project p = projectManager.getProject(id, false);
        if(p != null)
        {
            dashboardConfig = connfigurationProvider.deepClone(dashboardConfig);
            if(dashboardConfig.isShowAllProjects())
            {
                dashboardConfig.setShowAllProjects(false);
                dashboardConfig.getShownProjects().addAll(projectManager.getAllProjectConfigs(true));
            }

            dashboardConfig.getShownProjects().remove(p.getConfig());
        }

        userManager.save(user);
        return SUCCESS;
    }

    public void setConnfigurationProvider(ConfigurationProvider connfigurationProvider)
    {
        this.connfigurationProvider = connfigurationProvider;
    }
}
