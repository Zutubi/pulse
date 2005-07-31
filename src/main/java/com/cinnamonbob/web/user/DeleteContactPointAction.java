package com.cinnamonbob.web.user;

import com.cinnamonbob.model.ContactPoint;
import com.cinnamonbob.model.User;

/**
 *
 *
 */
public class DeleteContactPointAction extends UserActionSupport
{
    private long id;
    private String name;

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return this.id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String execute()
    {
        User user = getUserManager().getUser(id);
        if (user == null)
        {
            return INPUT;
        }

        ContactPoint contactPoint = user.getContactPoint(name);
        if (contactPoint != null)
        {
            user.remove(contactPoint);
        }
        return SUCCESS;
    }
}
