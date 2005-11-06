package com.cinnamonbob.web.user;

import com.cinnamonbob.web.ActionSupport;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.model.SubscriptionManager;
import com.cinnamonbob.xwork.interceptor.Cancelable;

/**
 *
 * 
 */
public class UserActionSupport extends ActionSupport implements Cancelable
{
    private String cancel;

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

    public boolean isCancelled()
    {
        return cancel != null;
    }

    public void setCancel(String name)
    {
        cancel = name;
    }
}
