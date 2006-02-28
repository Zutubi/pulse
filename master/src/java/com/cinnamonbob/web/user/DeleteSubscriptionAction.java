package com.cinnamonbob.web.user;

import com.cinnamonbob.model.Subscription;
import com.cinnamonbob.web.admin.user.UserActionSupport;

/**
 *
 *
 */
public class DeleteSubscriptionAction extends UserActionSupport
{
    private long id;

    private String login;

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
