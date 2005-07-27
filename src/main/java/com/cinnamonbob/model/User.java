package com.cinnamonbob.model;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 *
 */
public class User extends Entity
{
    private String login;
    private String name;

//    private Map<String, ContactPoint> contactPoints = new HashMap<String, ContactPoint>();
    private List<ContactPoint> contactPoints;

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

    public void add(AbstractContactPoint point)
    {
        contactPoints.add(point);
        point.setUser(this);
    }

    private void setContactPoints(List<ContactPoint> l)
    {
        this.contactPoints = l;
    }

    public List<ContactPoint> getContactPoints()
    {
        if (contactPoints == null)
        {
            contactPoints = new LinkedList<ContactPoint>();
        }
        return contactPoints;
    }

    //TODO: may want to look into using hibernate to store a map of
    //TODO: name -> contactpoint. 
    public ContactPoint getContactPoint(String name)
    {
        for (ContactPoint cp: contactPoints)
        {
            if (cp.getName().compareTo(name) == 0)
            {
                return cp;
            }
        }
        return null;
    }
}
