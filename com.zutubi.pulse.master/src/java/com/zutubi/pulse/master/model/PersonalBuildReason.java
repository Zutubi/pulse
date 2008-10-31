package com.zutubi.pulse.master.model;

/**
 */
public class PersonalBuildReason extends AbstractBuildReason
{
    private String user;

    public PersonalBuildReason()
    {
    }

    public PersonalBuildReason(String user)
    {
        this.user = user;
    }

    public String getSummary()
    {
        return "personal build for '" + user + "'";
    }

    public String getUser()
    {
        return user;
    }

    private void setUser(String user)
    {
        this.user = user;
    }
}
