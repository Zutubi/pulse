package com.cinnamonbob.web.user;

import com.cinnamonbob.model.EmailContactPoint;

/**
 *
 */
public class EditEmailContactPointAction extends UserActionSupport
{
    private long id;
    private long user;
    private EmailContactPoint contact = new EmailContactPoint();

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return this.id;
    }

    public long getUser()
    {
        return user;
    }

    public void setUser(long user)
    {
        this.user = user;
    }

    public EmailContactPoint getContact()
    {
        return contact;
    }

    public void validate()
    {

    }

    public String doDefault()
    {
        contact = (EmailContactPoint) getUserManager().getContactPoint(id);
        return SUCCESS;
    }

    public String execute()
    {
        EmailContactPoint persistentContact = (EmailContactPoint) getUserManager().getContactPoint(id);
        persistentContact.setEmail(contact.getEmail());
        persistentContact.setName(contact.getName());
        return SUCCESS;
    }

}
