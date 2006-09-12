package com.zutubi.validation.sample;

import com.zutubi.validation.annotations.Required;

/**
 * <class-comment/>
 */
public class Jabber
{
    private String host;

    public String getHost()
    {
        return host;
    }

    @Required
    public void setHost(String host)
    {
        this.host = host;
    }
}
