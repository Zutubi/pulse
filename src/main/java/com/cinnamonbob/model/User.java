package com.cinnamonbob.model;

import com.cinnamonbob.core.model.Entity;

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

    public void add(ContactPoint point)
    {
        contactPoints.add(point);
        point.setUser(this);
    }

    public boolean remove(ContactPoint point)
    {
        if (contactPoints.remove(point))
        {
            point.setUser(null);
            return true;
        }
        return false;
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

    public List<Subscription> getSubscriptions()
    {
        List<Subscription> subscriptions = new LinkedList<Subscription>();
        for (ContactPoint cp: contactPoints)
        {
            subscriptions.addAll(cp.getSubscriptions());
        }
        return subscriptions;
    }
}
