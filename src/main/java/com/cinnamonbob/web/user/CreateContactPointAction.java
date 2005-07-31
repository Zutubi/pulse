package com.cinnamonbob.web.user;

import com.cinnamonbob.model.EmailContactPoint;
import com.cinnamonbob.model.ContactPoint;
import com.cinnamonbob.model.User;

/**
 *
 *
 */
public class CreateContactPointAction extends UserActionSupport
{
    private long id;
    private EmailContactPoint contact = new EmailContactPoint();

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        User user = getUser();
        if (user == null)
        {
            addFieldError("id", "Unknown user["+id+"]");
            return;
        }

        if (user.getContactPoint(contact.getName()) != null)
        {
            addFieldError("contact.name", "Name is already in use.");
        }
    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
    }

    public User getUser()
    {
        return getUserManager().getUser(id);
    }

    public ContactPoint getContact()
    {
        return contact;
    }

    public String execute()
    {
        User user = getUser();
        user.add(contact);

        return SUCCESS;
    }
}
