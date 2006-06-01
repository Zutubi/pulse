package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.Subscription;

/**
 * <class-comment/>
 */
public class EditSubscriptionAction extends SubscriptionActionSupport
{
    private long id;
    private Subscription subscription;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String doInput()
    {
        setup();
        if(hasErrors())
        {
            return ERROR;
        }

        lookupSubscription();
        if(hasErrors())
        {
            return ERROR;
        }

        projectId = subscription.getProject().getId();
        contactPointId = subscription.getContactPoint().getId();
        condition = subscription.getCondition();

        return INPUT;
    }

    public void validate()
    {
        lookupUser();
        if(hasErrors())
        {
            return;
        }

        lookupSubscription();
        if(hasErrors())
        {
            return;
        }

        super.validate();
    }

    public String execute()
    {
        subscription.setProject(project);
        subscription.setContactPoint(contactPoint);
        subscription.setCondition(condition);
        getSubscriptionManager().save(subscription);
        return SUCCESS;
    }

    private void lookupSubscription()
    {
        subscription = user.getSubscription(id);
        if(subscription == null)
        {
            addActionError("Unknown subscription [" + id + "]");
        }
    }
}
