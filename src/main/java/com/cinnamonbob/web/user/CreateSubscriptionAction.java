package com.cinnamonbob.web.user;

import com.cinnamonbob.model.ProjectManager;
import com.cinnamonbob.model.Project;
import com.cinnamonbob.model.User;

/**
 *
 *
 */
public class CreateSubscriptionAction extends UserActionSupport
{
    private String projectName;
    private String userName;

    private ProjectManager projectManager;

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public ProjectManager getProjectManager()
    {
        return projectManager;
    }

    public String getProject()
    {
        return projectName;
    }

    public void setProject(String project)
    {
        this.projectName = project;
    }

    public String getUser()
    {
        return userName;
    }

    public void setUser(String user)
    {
        this.userName = user;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }
        // validate that project and user names reference existing entities.
        Project project = getProjectManager().getProject(projectName);
        if (project == null)
        {
            addFieldError("project", "Unknown project '"+projectName +"'");
        }
        User user = getUserManager().getUser(userName);
        if (user == null)
        {
            addFieldError("user", "Unknown user '"+userName +"'");
        }

        if (hasFieldErrors())
        {
            return;
        }

        // validate that the user has configured contact points.
        if (user.getContactPoints().size() == 0)
        {
            addFieldError("user", "This user does not have any contact points available. " +
                    "Please configure contact points before creating a subscription.");
        }
    }

    public String doDefault()
    {
        return SUCCESS;
    }

    public String execute()
    {
        Project project = getProjectManager().getProject(projectName);
        User user = getUserManager().getUser(userName);

        
        return SUCCESS;
    }

}
