package com.zutubi.pulse.notifications;

import com.zutubi.config.annotations.Form;
import com.zutubi.config.annotations.Text;
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

    @Required @Text(size= 60)
    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Required @Email @Text(size= 60)
    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }
}
