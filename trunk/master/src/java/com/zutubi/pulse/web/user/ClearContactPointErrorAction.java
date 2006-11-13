package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.ContactPoint;
import com.zutubi.pulse.model.EmailContactPoint;
import com.zutubi.pulse.model.JabberContactPoint;

/**
 *
 *
 */
public class ClearContactPointErrorAction extends UserActionSupport
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

    public String execute()
    {
        ContactPoint contact = getUserManager().getContactPoint(id);
        if (contact != null)
        {
            contact.clearError();
            getUserManager().save(contact);
        }
        
        return SUCCESS;
    }
}
