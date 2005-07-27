package com.cinnamonbob.model;

import java.util.Properties;

/**
 *
 *
 */
public abstract class AbstractContactPoint extends Entity implements ContactPoint {

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
}
