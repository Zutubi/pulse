package com.zutubi.pulse.form.sample;

import com.zutubi.pulse.form.persistence.Copyable;
import com.zutubi.pulse.form.descriptor.annotation.Field;
import com.zutubi.pulse.form.descriptor.annotation.Form;

import java.util.Collection;
import java.util.List;
import java.util.LinkedList;

/**
 * <class-comment/>
 */
@Form(fieldOrder = {"host", "user", "password", "format", "show"})
public class Jabber implements Copyable
{
    private String host = "initial host";
    private String user = "initial user";
    private String password;

    private String format;

    private boolean show;

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getFormat()
    {
        return format;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }

    public boolean isShow()
    {
        return show;
    }

    public void setShow(boolean show)
    {
        this.show = show;
    }

    public Collection<String> getFormatOptions()
    {
        List<String> options = new LinkedList<String>();
        options.add("email");
        options.add("html");
        options.add("plain");
        return options;
    }

    public Copyable copy()
    {
        Jabber copy = new Jabber();
        copy.user = this.user;
        copy.password = this.password;
        copy.host = this.host;
        return copy;
    }
}
