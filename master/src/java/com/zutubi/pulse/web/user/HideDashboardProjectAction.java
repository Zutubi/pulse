package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.Project;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.security.AcegiUtils;

/**
 * Action allowing a user to hide a chosen project from their dashboard.
 */
public class HideDashboardProjectAction extends UserActionSupport
{
    private long id;
    private ProjectManager projectManager;

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
        String login = AcegiUtils.getLoggedInUser();
        if (login == null)
        {
            return ERROR;
        }

        setUserLogin(login);

        User user = getUser();

        Project p = projectManager.getProject(id);
        if(p != null)
        {
            if(user.getShowAllProjects())
            {
                user.setShowAllProjects(false);
                user.getShownProjects().addAll(projectManager.getAllProjects());
            }

            user.getShownProjects().remove(p);
        }

        getUserManager().save(user);
        return SUCCESS;
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }
}
