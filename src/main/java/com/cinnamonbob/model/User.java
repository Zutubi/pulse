package com.cinnamonbob.model;

import java.util.Map;
import java.util.HashMap;

/**
 * 
 *
 */
public class User extends Entity
{
    private String login;
    private String name;
    
    private Map<String, ContactPoint> contactPoints = new HashMap<String, ContactPoint>();

    public String getLogin()
    {
        return login;
    }

    public void setLogin(String login)
    {
        this.login = login;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    
    public void add(String name, ContactPoint point)
    {
        contactPoints.put(name, point);
    }
}
