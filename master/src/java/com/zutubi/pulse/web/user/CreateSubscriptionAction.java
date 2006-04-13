/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.user;

import com.zutubi.pulse.ProjectNameComparator;
import com.zutubi.pulse.jabber.JabberManager;
import com.zutubi.pulse.bootstrap.ConfigurationManager;
import com.zutubi.pulse.model.*;
import com.opensymphony.util.TextUtils;

import java.util.Collections;
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
    private List<Project> projects;
    private Map<Long, String> contactPoints;

    private Project project;
    private ContactPoint contactPoint;
    private ConfigurationManager configurationManager;
    private JabberManager jabberManager;

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
            conditions.put(NotifyConditionFactory.ALL_FAILED_AND_FIRST_SUCCESS, getText("condition.allfailedandfirstsuccess"));
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

    public List<Project> getProjects()
    {
        return projects;
    }

    public Map<Long, String> getContactPoints()
    {
        return contactPoints;
    }

    public String doInput()
    {
        projects = projectManager.getAllProjects();
        if (projects.size() == 0)
        {
            addActionError("No projects available.  Please configure a project before creating a subscription.");
            return ERROR;
        }

        Collections.sort(projects, new ProjectNameComparator());

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

        if (!TextUtils.stringSet(configurationManager.getAppConfig().getSmtpHost()) && jabberManager.getConnection() == null)
        {
            addActionError("Unable to create a subscription as this server does not have an SMTP or Jabber host configured.");
            return ERROR;
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

    public void setJabberManager(JabberManager jabberManager)
    {
        this.jabberManager = jabberManager;
    }
}
