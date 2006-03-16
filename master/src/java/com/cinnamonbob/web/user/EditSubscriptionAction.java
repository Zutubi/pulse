package com.cinnamonbob.web.user;

import com.cinnamonbob.model.NotifyConditionFactory;
import com.cinnamonbob.model.Subscription;

import java.util.HashMap;
import java.util.Map;

/**
 * <class-comment/>
 */
public class EditSubscriptionAction extends UserActionSupport
{
    private long id;

    private String project;
    private String condition;
    private String contactPoint;

    private Map<String, String> conditions;

    public Map getConditions()
    {
        if (conditions == null)
        {
            conditions = new HashMap<String, String>();
            conditions.put(NotifyConditionFactory.ALL_BUILDS, getText("condition.allbuilds"));
            conditions.put(NotifyConditionFactory.ALL_CHANGED, getText("condition.allchanged"));
            conditions.put(NotifyConditionFactory.ALL_FAILED, getText("condition.allfailed"));
            conditions.put(NotifyConditionFactory.ALL_CHANGED_OR_FAILED, getText("condition.allchangedorfailed"));
        }
        return conditions;
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public void validate()
    {
        if (getSubscriptionManager().getSubscription(getId()) == null)
        {
            addFieldError("id", "Subscription referenced by id '"+getId()+"' could not be found.");
        }
    }

    public String doInput()
    {
        Subscription subscription = getSubscriptionManager().getSubscription(getId());
        project = subscription.getProject().getName();
        contactPoint = subscription.getContactPoint().getName();
        condition = subscription.getCondition();

        return INPUT;
    }

    public String execute()
    {
        // update condition.
        Subscription persistentSubscription = getSubscriptionManager().getSubscription(getId());
        persistentSubscription.setCondition(condition);
        getSubscriptionManager().save(persistentSubscription);

        return SUCCESS;
    }

    public String getProject()
    {
        return project;
    }

    public String getContactPoint()
    {
        return contactPoint;
    }

    public String getCondition()
    {
        return condition;
    }

    public void setProject(String str)
    {
        this.project = str;
    }

    public void setContactPoint(String str)
    {
        this.contactPoint = str;
    }

    public void setCondition(String str)
    {
        this.condition = str;
    }
}
