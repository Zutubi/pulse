package com.zutubi.pulse.notifications;

import com.zutubi.validation.annotations.Required;
import com.zutubi.pulse.form.descriptor.annotation.Form;
import com.zutubi.pulse.form.descriptor.annotation.Field;

/**
 * <class-comment/>
 */
@Form(fieldOrder = {"name", "username"})
public class JabberNotificationHandler implements NotificationHandler
{
    private String name;
    private String username;

    @Required public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    @Required public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
}
