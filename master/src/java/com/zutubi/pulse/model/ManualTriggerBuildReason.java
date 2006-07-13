package com.zutubi.pulse.model;

/**
 * Indicates that the build occured because a user manually triggered it.
 */
public class ManualTriggerBuildReason extends AbstractBuildReason
{
    private String username;

    public ManualTriggerBuildReason()
    {
    }

    public ManualTriggerBuildReason(String username)
    {
        this.username = username;
    }

    public String getSummary()
    {
        return "manual trigger by " + username;
    }

    public String getUsername()
    {
        return username;
    }

    private void setUsername(String username)
    {
        this.username = username;
    }
}
