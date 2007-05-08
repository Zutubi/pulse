package com.zutubi.pulse.web.user;

import com.zutubi.pulse.condition.NotifyConditionFactory;
import com.zutubi.pulse.model.*;
import com.zutubi.pulse.renderer.BuildResultRenderer;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 *
 */
public abstract class SubscriptionActionSupport extends UserActionSupport
{
    protected long id;
    protected long contactPointId;
    protected Map<Long, String> contactPoints;

    protected boolean personal;
    protected User user;
    protected ContactPoint contactPoint;
    protected String template;
    protected SubscriptionHelper helper;

    private NotifyConditionFactory notifyConditionFactory;
    private BuildResultRenderer buildResultRenderer;

    protected SubscriptionActionSupport(boolean personal)
    {
        this.personal = personal;
    }

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
            contactPoints = new LinkedHashMap<Long, String>();
            List<ContactPoint> contactPoints = user.getContactPoints();
            Collections.sort(contactPoints, new NamedEntityComparator());

            for (ContactPoint contact : contactPoints)
            {
                this.contactPoints.put(contact.getId(), contact.getName());
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
        helper = new SubscriptionHelper(personal, contactPoint, projectManager, notifyConditionFactory, this, buildResultRenderer);
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

    public void setNotifyConditionFactory(NotifyConditionFactory notifyConditionFactory)
    {
        this.notifyConditionFactory = notifyConditionFactory;
    }

    public void setBuildResultRenderer(BuildResultRenderer buildResultRenderer)
    {
        this.buildResultRenderer = buildResultRenderer;
    }
}