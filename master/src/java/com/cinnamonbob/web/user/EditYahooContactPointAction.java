package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.YahooContactPoint;

/**
 *
 *
 */
public class EditYahooContactPointAction extends AbstractEditContactPointAction
{
    private YahooContactPoint contact = new YahooContactPoint();

    public YahooContactPoint getContact()
    {
        return contact;
    }

    public String doInput()
    {
        contact = (YahooContactPoint) getUserManager().getContactPoint(getId());
        return INPUT;
    }

    public String execute()
    {
        YahooContactPoint persistentContact = (YahooContactPoint) getUserManager().getContactPoint(getId());
        persistentContact.setYahooId(contact.getYahooId());
        persistentContact.setName(contact.getName());
        return SUCCESS;
    }
}
