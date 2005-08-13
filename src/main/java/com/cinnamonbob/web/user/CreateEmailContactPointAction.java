package com.cinnamonbob.web.user;

import com.cinnamonbob.model.ContactPoint;
import com.cinnamonbob.model.EmailContactPoint;
import com.cinnamonbob.model.User;

/**
 *
 *
 *
 */
public class CreateEmailContactPointAction extends AbstractCreateContactPointAction
{
    private EmailContactPoint contact = new EmailContactPoint();

    public ContactPoint getContact()
    {
        return contact;
    }

    public String execute()
    {
        User user = getUserManager().getUser(getUser());
        user.add(contact);

        return SUCCESS;
    }
}
