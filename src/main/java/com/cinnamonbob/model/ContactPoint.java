package com.cinnamonbob.model;

import com.cinnamonbob.core.BuildResult;

import java.util.Properties;

/**
 * A contact point defines a means of contacting a user.
 */
public abstract class ContactPoint extends Entity
{
    private String name;

    private Properties properties;

    private String uid;

    private User user;

    public void setName(String name)
    {
        this.name = name;
    }

    public String getName()
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

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public abstract void notify(Project project, BuildResult result);
}

