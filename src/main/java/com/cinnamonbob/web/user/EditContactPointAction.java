package com.cinnamonbob.web.user;

import com.cinnamonbob.model.EmailContactPoint;
import com.cinnamonbob.model.ContactPoint;
import com.cinnamonbob.model.YahooContactPoint;

/**
 *
 *
 */
public class EditContactPointAction extends UserActionSupport
{
    private long id;
    private EmailContactPoint contact = new EmailContactPoint();

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return id;
    }

    public EmailContactPoint getContact()
    {
        return contact;
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
