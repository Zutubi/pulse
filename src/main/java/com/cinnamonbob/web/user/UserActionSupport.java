package com.cinnamonbob.web.user;

import com.cinnamonbob.web.ActionSupport;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.model.SubscriptionManager;

/**
 *
 * 
 */
public class UserActionSupport extends ActionSupport
{
    private UserManager userManager;

    private SubscriptionManager subscriptionManager;

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public UserManager getUserManager()
    {
        return userManager;
    }

    public SubscriptionManager getSubscriptionManager()
    {
        return subscriptionManager;
    }

    public void setSubscriptionManager(SubscriptionManager subscriptionManager)
    {
        this.subscriptionManager = subscriptionManager;
    }
}
