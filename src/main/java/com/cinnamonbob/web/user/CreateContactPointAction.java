package com.cinnamonbob.web.user;

import com.cinnamonbob.web.ActionSupport;
import com.cinnamonbob.model.EmailContactPoint;
import com.cinnamonbob.model.ContactPoint;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.model.User;

/**
 *
 *
 */
public class CreateContactPointAction extends ActionSupport
{
    private long id;
    private EmailContactPoint contact = new EmailContactPoint();

    private UserManager userManager;

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void validate()
    {
        if (hasErrors())
        {
            return;
        }

        User user = userManager.getUser(id);
        if (user == null)
        {
            addFieldError("id", "Unknown user["+id+"]");
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
        return userManager.getUser(id);
    }

    public ContactPoint getContact()
    {
        return contact;
    }

    public String execute()
    {
        User user = userManager.getUser(id);
        user.add(contact);

        return SUCCESS;
    }
}
