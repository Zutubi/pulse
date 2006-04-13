/********************************************************************************
 @COPYRIGHT@
 ********************************************************************************/
package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.EmailContactPoint;

/**
 *
 */
public class EditEmailContactPointAction extends AbstractEditContactPointAction
{
    private EmailContactPoint contact = new EmailContactPoint();

    public EmailContactPoint getContact()
    {
        return contact;
    }

    public String doInput()
    {
        contact = (EmailContactPoint) getUserManager().getContactPoint(getId());
        return INPUT;
    }

    public String execute()
    {
        EmailContactPoint persistentContact = (EmailContactPoint) getUserManager().getContactPoint(getId());
        persistentContact.setEmail(contact.getEmail());
        persistentContact.setName(contact.getName());
        getUserManager().save(persistentContact);
        return SUCCESS;
    }

}
