package com.cinnamonbob.web.user;

import com.cinnamonbob.model.ContactPoint;
import com.cinnamonbob.model.EmailContactPoint;
import com.cinnamonbob.model.User;

/**
 *
 *
 *
 */
public class CreateEmailContactPointAction extends UserActionSupport
{
    private long user;

    private EmailContactPoint contact = new EmailContactPoint();

    public long getUser()
    {
        return user;
    }

    public void setUser(long user)
    {
        this.user = user;
    }

    public ContactPoint getContact()
    {
        return contact;
    }

    public void validate()
    {

    }

    public String execute()
    {
        User user = getUserManager().getUser(getUser());
        user.add(contact);

        return SUCCESS;
    }
}
