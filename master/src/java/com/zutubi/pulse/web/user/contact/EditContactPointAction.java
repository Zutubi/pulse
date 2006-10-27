package com.zutubi.pulse.web.user.contact;

import com.zutubi.pulse.model.*;
import com.zutubi.pulse.notifications.EmailNotificationHandler;
import com.zutubi.pulse.notifications.JabberNotificationHandler;
import com.zutubi.validation.Validateable;
import com.zutubi.validation.ValidationContext;

/**
 * <class comment/>
 */
public class EditContactPointAction extends FormAction implements Validateable
{
    private UserManager userManager;

    protected long id;

    private long userId;

    private String name;

    public void setId(long id)
    {
        this.id = id;
    }

    public long getId()
    {
        return this.id;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public void validate(ValidationContext context)
    {
        if (context.hasErrors())
        {
            return;
        }
        
        User user = userManager.getUser(userId);
        ContactPoint contact = user.getContactPoint(name);
        if (contact != null && contact.getId() != id)
        {
            addFieldError("name", context.getText("name.invalid"));
        }
    }

    public Object doLoad()
    {
        ContactPoint contact = userManager.getUser(userId).getContactPoint(id);
        if (contact instanceof EmailContactPoint)
        {
            EmailNotificationHandler emailHandler = new EmailNotificationHandler();
            emailHandler.setName(contact.getName());
            emailHandler.setEmail(((EmailContactPoint)contact).getEmail());
            return emailHandler;
        }
        else if (contact instanceof JabberContactPoint)
        {
            JabberNotificationHandler jabberHandler = new JabberNotificationHandler();
            jabberHandler.setName(contact.getName());
            jabberHandler.setUsername(((JabberContactPoint)contact).getUsername());
            return jabberHandler;
        }
        return null;
    }

    public void doSave(Object obj)
    {
        ContactPoint contact = userManager.getUser(userId).getContactPoint(id);
        if (contact instanceof EmailContactPoint)
        {
            EmailNotificationHandler emailHandler = (EmailNotificationHandler) obj;
            contact.setName(emailHandler.getName());
            ((EmailContactPoint)contact).setEmail(emailHandler.getEmail());
        }
        else if (contact instanceof JabberContactPoint)
        {
            JabberNotificationHandler jabberHandler = (JabberNotificationHandler) obj;
            contact.setName(jabberHandler.getName());
            ((JabberContactPoint)contact).setUsername(jabberHandler.getUsername());
        }
        userManager.save(contact);
    }

    public void setUserManager(UserManager userManager)
    {
        this.userManager = userManager;
    }

    public void setUserId(long userId)
    {
        this.userId = userId;
    }

    public long getUserId()
    {
        return userId;
    }
}
