package com.cinnamonbob.web.user;

import com.cinnamonbob.model.*;
import com.opensymphony.util.TextUtils;

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
    private String contactPoint;
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
            conditions.put(NotifyConditionFactory.ALL_BUILDS, "All builds");
            conditions.put(NotifyConditionFactory.ALL_CHANGED, "All changed builds");
            conditions.put(NotifyConditionFactory.ALL_FAILED, "All failed builds");
            conditions.put(NotifyConditionFactory.ALL_CHANGED_AND_FAILED, "All changed or failed builds");
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

    public String getContactPoint()
    {
        return contactPoint;
    }

    public void setContactPoint(String str)
    {
        this.contactPoint = str;
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
            return;
        }

        if (TextUtils.stringSet(contactPoint) && user.getContactPoint(contactPoint) == null)
        {
            addFieldError("contactPoint", "The contact point you have specified does not exist for this user. " +
                    "Please specify another contact point.");
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

        ContactPoint cp = null;
        if (TextUtils.stringSet(contactPoint))
        {
            cp = user.getContactPoint(contactPoint);
        }
        else
        {
            cp = user.getContactPoints().get(0);
        }

        Subscription subscription = new Subscription(project, cp);
        subscription.setCondition(condition);

        getSubscriptionManager().save(subscription);

        return SUCCESS;
    }

}
