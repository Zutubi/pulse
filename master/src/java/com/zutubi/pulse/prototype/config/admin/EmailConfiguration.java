package com.zutubi.pulse.prototype.config.admin;

/**
 *
 *
 */
public class EmailConfiguration
{
    private String host;
    private String username;
    private String subjectPrefix;
    private String from;

    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getSubjectPrefix()
    {
        return subjectPrefix;
    }

    public void setSubjectPrefix(String subjectPrefix)
    {
        this.subjectPrefix = subjectPrefix;
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }
}
