package com.zutubi.pulse.model;

import com.zutubi.pulse.core.model.Entity;
import com.zutubi.validation.annotations.Required;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * A contact point defines a means of contacting a user.
 */
public abstract class ContactPoint extends Entity implements NamedEntity
{
    private String name;
    private Properties properties;
    private String uid;
    private User user;
    private List<Subscription> subscriptions;
    private String lastError;

    public void setName(String name)
    {
        this.name = name;
    }

    @Required public String getName()
    {
        return this.name;
    }

    private void setProperties(Properties properties)
    {
        this.properties = properties;
    }

    protected Properties getProperties()
    {
        if (properties == null)
        {
            properties = new Properties();
        }
        return this.properties;
    }

    protected String getStringProperty(String name)
    {
        return (String) getProperties().get(name);
    }
    
    public String getUid()
    {
        return uid;
    }

    public void setUid(String uid)
    {
        this.uid = uid;
    }

    public User getUser()
    {
        return user;
    }

    public void setUser(User user)
    {
        this.user = user;
    }

    public String getLastError()
    {
        return lastError;
    }

    public boolean hasError()
    {
        return lastError != null;
    }

    private void setLastError(String lastError)
    {
        this.lastError = lastError;
    }

    public void clearError()
    {
        lastError = null;
    }

    public List<Subscription> getSubscriptions()
    {
        if (subscriptions == null)
        {
            subscriptions = new LinkedList<Subscription>();
        }
        return subscriptions;
    }

    public void add(Subscription subscription)
    {
        if (!getSubscriptions().contains(subscription))
        {
            getSubscriptions().add(subscription);
            subscription.setContactPoint(this);
        }
    }

    public void remove(Subscription subscription)
    {
        if (getSubscriptions().contains(subscription))
        {
            getSubscriptions().remove(subscription);
            subscription.setContactPoint(null);
        }
    }

    /**
     * Used by hibernate only.
     *
     * @param subscriptions
     */
    private void setSubscriptions(List<Subscription> subscriptions)
    {
        this.subscriptions = subscriptions;
    }

    public void notify(BuildResult result, String subject, String rendered, String mimeType)
    {
        lastError = null;
        try
        {
            internalNotify(result, subject, rendered, mimeType);
        }
        catch(Exception e)
        {
            lastError = e.getClass().getName();
            if(e.getMessage() != null)
            {
                lastError += ": " + e.getMessage();
            }
        }
    }

    public abstract String getDefaultTemplate();

    protected abstract void internalNotify(BuildResult result, String subj, String rendered, String mimeType) throws Exception;
}

