package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.PersonalBuildSubscription;
import com.zutubi.pulse.model.Subscription;

/**
 * <class-comment/>
 */
public class EditPersonalBuildSubscriptionAction extends SubscriptionActionSupport
{
    private PersonalBuildSubscription subscription;

    public EditPersonalBuildSubscriptionAction()
    {
        super(true);
    }

    public String doInput() throws Exception
    {
        super.doInput();
        if(hasErrors())
        {
            return ERROR;
        }

        lookupSubscription();
        if(hasErrors())
        {
            return ERROR;
        }

        contactPointId = subscription.getContactPoint().getId();
        template = subscription.getTemplate();
        return INPUT;
    }

    public void validate()
    {
        if (lookupUser())
        {
            return;
        }

        if(lookupSubscription() == null)
        {
            return;
        }

        super.validate();
    }

    protected Subscription lookupSubscription()
    {
        Subscription s = user.getSubscription(id);
        if(s == null || !(s instanceof PersonalBuildSubscription))
        {
            addActionError("Unknown subscription [" + id + "]");
            return null;
        }
        else
        {
            subscription = (PersonalBuildSubscription) s;
            return s;
        }
    }

    public String execute()
    {
        subscription.setContactPoint(contactPoint);
        subscription.setTemplate(getTemplate());
        getSubscriptionManager().save(subscription);
        return SUCCESS;
    }
}
