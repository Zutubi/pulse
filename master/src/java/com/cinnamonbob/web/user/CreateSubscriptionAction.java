package com.cinnamonbob.web.user;

import com.cinnamonbob.model.*;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 *
 */
public class CreateSubscriptionAction extends UserActionSupport
{
    private ProjectManager projectManager;

    private long userId;
    private long projectId;
    private long contactPointId;
    private String condition;

    private Map<String, String> conditions;
    private Map<Long, String> projects;
    private Map<Long, String> contactPoints;

    private User user;
    private Project project;
    private ContactPoint contactPoint;

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
            conditions.put(NotifyConditionFactory.ALL_BUILDS, "all builds");
            conditions.put(NotifyConditionFactory.ALL_CHANGED, "all changed builds");
            conditions.put(NotifyConditionFactory.ALL_FAILED, "all failed builds");
            conditions.put(NotifyConditionFactory.ALL_CHANGED_OR_FAILED, "all changed or failed builds");
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

    public long getUserId()
    {
        return userId;
    }

    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    public long getProjectId()
    {
        return projectId;
    }

    public void setProjectId(long projectId)
    {
        this.projectId = projectId;
    }

    public long getContactPointId()
    {
        return contactPointId;
    }

    public void setContactPointId(long contactPointId)
    {
        this.contactPointId = contactPointId;
    }

    public Map<Long, String> getProjects()
    {
        return projects;
    }

    public Map<Long, String> getContactPoints()
    {
        return contactPoints;
    }

    public String doDefault()
    {
        List<Project> allProjects = projectManager.getAllProjects();
        if(allProjects.size() == 0)
        {
            addActionError("No projects available.  Please configure a project before creating a subscription.");
            return ERROR;
        }

        user = getUserManager().getUser(userId);
        if(user == null)
        {
            addActionError("Unknown user '" + userId + "'");
            return ERROR;
        }

        // validate that the userId has configured contact points.
        if (user.getContactPoints().size() == 0)
        {
            addFieldError("userId", "This userId does not have any contact points available. " +
                    "Please configure contact points before creating a subscription.");
            return ERROR;
        }

        projects = new TreeMap<Long, String>();
        for(Project project: allProjects)
        {
            projects.put(project.getId(), project.getName());
        }

        contactPoints = new TreeMap<Long, String>();
        for(ContactPoint contact: user.getContactPoints())
        {
            contactPoints.put(contact.getId(), contact.getName());
        }

        return SUCCESS;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        user = getUserManager().getUser(userId);
        if (user == null)
        {
            addActionError("Unknown user '" + userId + "'");
        }

        project = getProjectManager().getProject(projectId);
        if (project == null)
        {
            addFieldError("projectId", "Unknown project '"+ projectId +"'");
        }

        contactPoint = user.getContactPoint(contactPointId);
        if (contactPoint == null)
        {
            addFieldError("contactPointId", "Unknown contact point '" + contactPointId + "' for user '" + user.getName() + "'");
        }
    }

    public String execute()
    {
        Subscription subscription = new Subscription(project, contactPoint);
        subscription.setCondition(condition);
        getSubscriptionManager().save(subscription);

        return SUCCESS;
    }

}
