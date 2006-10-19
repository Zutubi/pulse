package com.zutubi.pulse.web.user;

import com.zutubi.pulse.condition.NotifyConditionFactory;
import com.zutubi.pulse.model.ContactPoint;
import com.zutubi.pulse.model.ProjectManager;
import com.zutubi.pulse.model.Subscription;
import com.zutubi.pulse.model.User;
import com.zutubi.pulse.renderer.BuildResultRenderer;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 *
 */
public abstract class SubscriptionActionSupport extends UserActionSupport
{
    protected long id;
    protected long contactPointId;
    protected Map<Long, String> contactPoints;

    protected User user;
    protected ContactPoint contactPoint;
    protected String template;
    protected SubscriptionHelper helper;

    private ProjectManager projectManager;
    private NotifyConditionFactory notifyConditionFactory;
    private BuildResultRenderer buildResultRenderer;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public long getContactPointId()
    {
        return contactPointId;
    }

    public void setContactPointId(long contactPointId)
    {
        this.contactPointId = contactPointId;
    }

    public Map<Long, String> getContactPoints()
    {
        if(contactPoints == null)
        {
            contactPoints = new TreeMap<Long, String>();
            for (ContactPoint contact : user.getContactPoints())
            {
                contactPoints.put(contact.getId(), contact.getName());
            }
        }

        return contactPoints;
    }

    public String getTemplate()
    {
        return template;
    }

    public void setTemplate(String template)
    {
        this.template = template;
    }

    public Map<String, String> getAvailableTemplates()
    {
        return helper.getAvailableTemplates();
    }

    public String doInput() throws Exception
    {
        user = getUser();
        if (user == null)
        {
            addUnknownUserActionError();
            return ERROR;
        }

        Subscription s = lookupSubscription();
        if(hasErrors())
        {
            return ERROR;
        }
        
        contactPoint = s.getContactPoint();
        createHelper();

        return INPUT;
    }

    protected void createHelper()
    {
        helper = new SubscriptionHelper(user, contactPoint, projectManager, notifyConditionFactory, this, buildResultRenderer);
    }

    protected boolean lookupUser()
    {
        user = getUser();
        if (user == null)
        {
            addUnknownUserActionError();
            return true;
        }
        return false;
    }

    protected abstract Subscription lookupSubscription();

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        contactPoint = user.getContactPoint(contactPointId);
        if (contactPoint == null)
        {
            addFieldError("contactPointId", "Unknown contact point '" + contactPointId + "' for user '" + user.getName() + "'");
        }
    }

    public void setProjectManager(ProjectManager projectManager)
    {
        this.projectManager = projectManager;
    }

    public void setNotifyConditionFactory(NotifyConditionFactory notifyConditionFactory)
    {
        this.notifyConditionFactory = notifyConditionFactory;
    }

    public void setBuildResultRenderer(BuildResultRenderer buildResultRenderer)
    {
        this.buildResultRenderer = buildResultRenderer;
    }
}