package com.zutubi.pulse.web.user;

import com.zutubi.pulse.model.JabberContactPoint;

/**
 *
 *
 */
public class EditJabberContactPointAction extends AbstractEditContactPointAction
{
    private JabberContactPoint contact = new JabberContactPoint();

    public JabberContactPoint getContact()
    {
        return contact;
    }

    public String doInput()
    {
        contact = (JabberContactPoint) getUserManager().getContactPoint(getId());
        return INPUT;
    }

    public String execute()
    {
        JabberContactPoint persistentContact = (JabberContactPoint) getUserManager().getContactPoint(getId());
        persistentContact.setUsername(contact.getUsername());
        persistentContact.setName(contact.getName());
        getUserManager().save(persistentContact);        
        return SUCCESS;
    }
}
