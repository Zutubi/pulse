package com.cinnamonbob.web.admin.user;

import com.cinnamonbob.model.ContactPoint;
import com.cinnamonbob.web.admin.user.UserActionSupport;

/**
 *
 *
 */
public class DeleteContactPointAction extends UserActionSupport
{
    private long id;
    private long user;

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return this.id;
    }

    public String execute()
    {

        ContactPoint contactPoint = getUserManager().getContactPoint(id);
        if (contactPoint != null)
        {
            contactPoint.getUser().remove(contactPoint);
            getUserManager().delete(contactPoint);
        }

        return SUCCESS;
    }

    public void setUser(long user)
    {
        this.user = user;
    }

    public long getUser()
    {
        return user;
    }
}
