package com.cinnamonbob.web.admin.user;

import com.cinnamonbob.model.EmailContactPoint;
import com.cinnamonbob.model.ContactPoint;
import com.cinnamonbob.model.YahooContactPoint;
import com.cinnamonbob.web.admin.user.UserActionSupport;

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
