package com.zutubi.pulse.master.model;

/**
 * Indicates that the build occurred because a user manually triggered it.
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

    public boolean isUser()
    {
        return true;
    }

    public String getSummary()
    {
        return "manual trigger by " + (username != null ? username : "anonymous");
    }

    public String getUsername()
    {
        return username;
    }

    /**
     * Used by hibernate.
     *
     * @param username property value.
     */
    private void setUsername(String username)
    {
        this.username = username;
    }
}
