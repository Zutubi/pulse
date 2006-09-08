package com.zutubi.pulse.form.sample;

import com.zutubi.pulse.form.persistence.Copyable;
import com.zutubi.pulse.form.descriptor.annotation.Field;

/**
 * <class-comment/>
 */
public class Jabber implements Copyable
{
    private String host = "initial host";
    private String user = "initial user";
    private String password;

    @Field(required = true)
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

    public Copyable copy()
    {
        Jabber copy = new Jabber();
        copy.user = this.user;
        copy.password = this.password;
        copy.host = this.host;
        return copy;
    }
}
