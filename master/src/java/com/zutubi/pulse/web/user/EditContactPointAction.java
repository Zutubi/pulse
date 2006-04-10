package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.ContactPoint;
import com.zutubi.pulse.model.EmailContactPoint;
import com.zutubi.pulse.model.YahooContactPoint;

/**
 *
 *
 */
public class EditContactPointAction extends UserActionSupport
{
    private long id;

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return id;
    }

    public String doDefault()
    {
        ContactPoint contact = getUserManager().getContactPoint(id);
        if (contact == null)
        {
            return INPUT;
        }
        if (contact instanceof YahooContactPoint)
        {
            return "yahoo";
        }
        else if (contact instanceof EmailContactPoint)
        {
            return "email";
        }
        return ERROR;
    }
}
