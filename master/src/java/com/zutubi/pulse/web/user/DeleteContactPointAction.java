package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.ContactPoint;

/**
 *
 *
 */
public class DeleteContactPointAction extends UserActionSupport
{
    private long id;

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
}
