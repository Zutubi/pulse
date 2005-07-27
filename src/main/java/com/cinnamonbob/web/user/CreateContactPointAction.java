package com.cinnamonbob.web.user;

import com.cinnamonbob.web.BaseActionSupport;
import com.cinnamonbob.model.EmailContactPoint;
import com.cinnamonbob.model.ContactPoint;
import com.cinnamonbob.model.UserManager;
import com.cinnamonbob.model.User;

/**
 *
 *
 */
public class CreateContactPointAction extends BaseActionSupport
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

    }

    public long getId()
    {
        return id;
    }

    public void setId(long id)
    {
        this.id = id;
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
