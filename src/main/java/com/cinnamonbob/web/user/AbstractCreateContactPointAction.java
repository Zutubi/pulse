package com.cinnamonbob.web.user;

import com.cinnamonbob.model.User;
import com.cinnamonbob.model.ContactPoint;

/**
 *
 *
 */
public abstract class AbstractCreateContactPointAction extends UserActionSupport
{
    private long user;

    public long getUser()
    {
        return user;
    }

    public void setUser(long user)
    {
        this.user = user;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }
        User user = getUserManager().getUser(getUser());
        ContactPoint contact = user.getContactPoint(getContact().getName());
        if (contact != null)
        {
            addFieldError("contact.name", "Please use a different name, this one is already in use.");
        }
    }

    public abstract ContactPoint getContact();
}
