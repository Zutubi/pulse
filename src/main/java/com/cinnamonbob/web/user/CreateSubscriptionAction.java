package com.cinnamonbob.web.user;

import com.cinnamonbob.model.*;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 *
 */
public class CreateSubscriptionAction extends UserActionSupport
{
    private ProjectManager projectManager;

    private String projectName;
    private String userName;

    private String condition;

    private Map<String, String> conditions;


    public String getCondition()
    {
        return condition;
    }

    public void setCondition(String condition)
    {
        this.condition = condition;
    }

    public Map getConditions()
    {
        if (conditions == null)
        {
            conditions = new TreeMap<String, String>();
            conditions.put("a", "All builds");
            conditions.put("b", "All changed builds");
            conditions.put("c", "All failed builds");
            conditions.put("d", "All changed or failed builds");
        }
        return conditions;
    }

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
        ContactPoint contactPoint = user.getContactPoints().get(0);

        Subscription subscription = new Subscription(project, contactPoint);
        // which notification condition do we want?
        if ("a".equals(condition))
        {

        }
        else if ("b".equals(condition))
        {

        }
        else if ("c".equals(condition))
        {

        }
        else if ("d".equals(condition))
        {

        }
        getSubscriptionManager().save(subscription);

        return SUCCESS;
    }

}
