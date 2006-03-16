package com.cinnamonbob.web.user;

import com.cinnamonbob.model.ContactPoint;
import com.cinnamonbob.model.User;

/**
 *
 *
 */
public abstract class AbstractEditContactPointAction extends UserActionSupport
{
    protected long id;
    protected long user;
    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return this.id;
    }

/*
    public long getUser()
    {
        return user;
    }

    public void setUser(long user)
    {
        this.user = user;
    }
*/

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }
        User user = getUser();
        ContactPoint contact = user.getContactPoint(getContact().getName());
        if (contact != null && contact.getId() != id)
        {
            addFieldError("contact.name", "Please use a different name, this one is already in use.");
        }
    }

    public abstract ContactPoint getContact();
}
