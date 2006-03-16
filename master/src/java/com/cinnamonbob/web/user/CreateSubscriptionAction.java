package com.cinnamonbob.web.user;

import com.cinnamonbob.bootstrap.ConfigurationManager;
import com.cinnamonbob.model.*;
import com.opensymphony.util.TextUtils;

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

    private long projectId;
    private long contactPointId;
    private String condition;

    private Map<String, String> conditions;
    private Map<Long, String> projects;
    private Map<Long, String> contactPoints;

    private Project project;
    private ContactPoint contactPoint;
    private ConfigurationManager configurationManager;

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
            conditions.put(NotifyConditionFactory.ALL_BUILDS, getText("condition.allbuilds"));
            conditions.put(NotifyConditionFactory.ALL_CHANGED, getText("condition.allchanged"));
            conditions.put(NotifyConditionFactory.ALL_FAILED, getText("condition.allfailed"));
            conditions.put(NotifyConditionFactory.ALL_CHANGED_OR_FAILED, getText("condition.allchangedorfailed"));
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

    public String doInput()
    {
        List<Project> allProjects = projectManager.getAllProjects();
        if (allProjects.size() == 0)
        {
            addActionError("No projects available.  Please configure a project before creating a subscription.");
            return ERROR;
        }

        User user = getUser();
        if (user == null)
        {
            addUnknownUserActionError();
            return ERROR;
        }

        // validate that the userId has configured contact points.
        if (user.getContactPoints().size() == 0)
        {
            addActionError("You do not have any contact points configured. " +
                    "Please configure a contact point before creating a subscription.");
            return ERROR;
        }

        if (!TextUtils.stringSet(configurationManager.getAppConfig().getSmtpHost()))
        {
            addActionError("Unable to create a subscription as this server does not have an SMTP host configured.");
            return ERROR;
        }

        projects = new TreeMap<Long, String>();
        for (Project project : allProjects)
        {
            projects.put(project.getId(), project.getName());
        }

        contactPoints = new TreeMap<Long, String>();
        for (ContactPoint contact : user.getContactPoints())
        {
            contactPoints.put(contact.getId(), contact.getName());
        }

        return INPUT;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        User user = getUser();
        if (user == null)
        {
            addUnknownUserActionError();
            return;
        }

        project = getProjectManager().getProject(projectId);
        if (project == null)
        {
            addFieldError("projectId", "Unknown project '" + projectId + "'");
            return;
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

    public void setConfigurationManager(ConfigurationManager configurationManager)
    {
        this.configurationManager = configurationManager;
    }
}
