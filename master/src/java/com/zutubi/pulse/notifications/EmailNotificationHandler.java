package com.zutubi.pulse.notifications;

import com.zutubi.pulse.form.descriptor.annotation.Form;
import com.zutubi.validation.annotations.Email;
import com.zutubi.validation.annotations.Required;

/**
 * <class-comment/>
 */
@Form(fieldOrder = {"name", "email"})
public class EmailNotificationHandler implements NotificationHandler
{
    private String email;
    private String name;

    @Required public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Required @Email public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }
}
