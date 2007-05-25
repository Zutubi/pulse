package com.zutubi.pulse.prototype.config.user.contacts;

/**
 *
 *
 */
public class JabberContactConfiguration extends ContactConfiguration
{
    private String username;

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getUid()
    {
        return getUsername();
    }
}
