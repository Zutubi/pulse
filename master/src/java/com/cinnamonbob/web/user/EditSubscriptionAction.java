package com.cinnamonbob.web.user;

import com.cinnamonbob.model.Subscription;
import com.cinnamonbob.model.NotifyConditionFactory;
import com.cinnamonbob.web.admin.user.UserActionSupport;

import java.util.Map;
import java.util.TreeMap;

/**
 * <class-comment/>
 */
public class EditSubscriptionAction extends UserActionSupport
{
    private long id;
    private String login;

    private String project;
    private String user;
    private String condition;
    private String contactPoint;

    private Map<String, String> conditions;

    public Map getConditions()
    {
        if (conditions == null)
        {
            conditions = new TreeMap<String, String>();
            conditions.put(NotifyConditionFactory.ALL_BUILDS, "All builds");
            conditions.put(NotifyConditionFactory.ALL_CHANGED, "All changed builds");
            conditions.put(NotifyConditionFactory.ALL_FAILED, "All failed builds");
            conditions.put(NotifyConditionFactory.ALL_CHANGED_OR_FAILED, "All changed or failed builds");
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

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public void validate()
    {
        if (getSubscriptionManager().getSubscription(getId()) == null)
        {
            addFieldError("id", "Subscription referenced by id '"+getId()+"' could not be found.");
        }
    }

    public String doDefault()
    {
        Subscription subscription = getSubscriptionManager().getSubscription(getId());
        project = subscription.getProject().getName();
        contactPoint = subscription.getContactPoint().getName();
        user = subscription.getContactPoint().getUser().getLogin();
        condition = subscription.getCondition();

        return SUCCESS;
    }

    public String execute()
    {
        Subscription persistentSubscription = getSubscriptionManager().getSubscription(getId());

        // update condition.
        persistentSubscription.setCondition(condition);

        return SUCCESS;
    }

    public String getProject()
    {
        return project;
    }

    public String getUser()
    {
        return user;
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

    public void setUser(String str)
    {
        this.user = str;
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
