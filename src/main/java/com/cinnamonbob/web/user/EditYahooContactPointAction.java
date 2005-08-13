package com.cinnamonbob.web.user;

import com.cinnamonbob.model.YahooContactPoint;

/**
 *
 *
 */
public class EditYahooContactPointAction extends UserActionSupport
{
    private long id;
    private long user;
    private YahooContactPoint contact = new YahooContactPoint();

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

    public YahooContactPoint getContact()
    {
        return contact;
    }

    public void validate()
    {

    }

    public String doDefault()
    {
        contact = (YahooContactPoint) getUserManager().getContactPoint(id);
        return SUCCESS;
    }

    public String execute()
    {
        YahooContactPoint persistentContact = (YahooContactPoint) getUserManager().getContactPoint(id);
        persistentContact.setYahooId(contact.getYahooId());
        persistentContact.setName(contact.getName());
        return SUCCESS;
    }

}
