package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.ContactPoint;
import com.zutubi.pulse.model.User;

/**
 *
 *
 */
public abstract class AbstractEditContactPointAction extends UserActionSupport
{
    protected long id;

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return this.id;
    }

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
