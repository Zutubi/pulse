package com.zutubi.pulse.master.model;

/**
 * Indicates that the build occurred because a user manually triggered it.  See also
 * {@link NamedManualTriggerBuildReason} which is used when a configured trigger is fired (so we
 * have both a trigger name and a username).
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
