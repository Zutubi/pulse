package com.zutubi.pulse.notifications;

import com.zutubi.validation.annotations.Required;
import com.zutubi.validation.annotations.Email;
import com.zutubi.pulse.form.descriptor.annotation.Radio;
import com.zutubi.pulse.form.descriptor.annotation.Form;

import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
@Form(fieldOrder = {"name", "email", "format"})
public class EmailNotificationHandler implements NotificationHandler
{
    private String email;
    private String format = "html";
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

    @Required @Radio(list = {"plain", "html"})
    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

}
