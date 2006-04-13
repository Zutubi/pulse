/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.Subscription;

/**
 *
 *
 */
public class DeleteSubscriptionAction extends UserActionSupport
{
    private long id;

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public String execute()
    {
        Subscription subscription = getSubscriptionManager().getSubscription(id);
        if (subscription != null)
        {
            getSubscriptionManager().delete(subscription);
        }
        return SUCCESS;
    }

}
